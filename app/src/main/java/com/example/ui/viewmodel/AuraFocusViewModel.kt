package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiProductivityCoach
import com.example.cloud.CloudSyncManager
import com.example.cloud.SyncResult
import com.example.cloud.SyncStatus
import com.example.data.db.AppDatabase
import com.example.data.model.AppBlockCategory
import com.example.data.model.BlockedAppEntity
import com.example.data.model.DailyStatEntity
import com.example.data.model.PomodoroSessionEntity
import com.example.data.model.ProductivityMetrics
import com.example.data.model.SessionMode
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItemEntity
import com.example.data.model.TaskPriority
import com.example.data.preferences.PreferencesManager
import com.example.data.preferences.UserSettings
import com.example.util.SoundSynthesizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TimerUiState(
    val mode: SessionMode = SessionMode.FOCUS,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val remainingSeconds: Int = 25 * 60,
    val totalDurationSeconds: Int = 25 * 60,
    val completedCycles: Int = 0,
    val activeTask: TaskItemEntity? = null,
    val distractionStrikes: Int = 0,
    val showDistractionShieldDialog: Boolean = false,
    val shieldTriggeredAppName: String? = null
)

class AuraFocusViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val taskDao = db.taskDao()
    private val sessionDao = db.sessionDao()
    private val blockedAppDao = db.blockedAppDao()
    private val dailyStatDao = db.dailyStatDao()
    val preferencesManager = PreferencesManager(application)
    val soundSynthesizer = SoundSynthesizer(application)
    private val aiCoach = AiProductivityCoach()
    val cloudSyncManager = CloudSyncManager(application, db, preferencesManager)

    // Flows from database
    val tasksFlow: StateFlow<List<TaskItemEntity>> = taskDao.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedAppsFlow: StateFlow<List<BlockedAppEntity>> = blockedAppDao.getAllBlockedAppsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSessionsFlow: StateFlow<List<PomodoroSessionEntity>> = sessionDao.getRecentSessionsFlow(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyStatsFlow: StateFlow<List<DailyStatEntity>> = dailyStatDao.getRecentDailyStatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settingsFlow: StateFlow<UserSettings> = preferencesManager.settingsFlow

    // Timer state
    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    // AI Coach state
    private val _aiResponseText = MutableStateFlow<String?>(null)
    val aiResponseText: StateFlow<String?> = _aiResponseText.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Cloud sync state
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    // Timer high-accuracy engine
    private var timerJob: Job? = null
    private var targetEndTimeMs: Long = 0L

    init {
        // Sync initial timer duration with settings
        val currentSettings = settingsFlow.value
        val initialSeconds = currentSettings.focusDurationMinutes * 60
        _timerState.value = _timerState.value.copy(
            remainingSeconds = initialSeconds,
            totalDurationSeconds = initialSeconds
        )

        // Listen for duration setting changes when idle
        viewModelScope.launch {
            settingsFlow.collect { settings ->
                if (!_timerState.value.isRunning && !_timerState.value.isPaused) {
                    val durationMins = when (_timerState.value.mode) {
                        SessionMode.FOCUS -> settings.focusDurationMinutes
                        SessionMode.SHORT_BREAK -> settings.shortBreakMinutes
                        SessionMode.LONG_BREAK -> settings.longBreakMinutes
                    }
                    val sec = durationMins * 60
                    _timerState.value = _timerState.value.copy(
                        remainingSeconds = sec,
                        totalDurationSeconds = sec
                    )
                }
            }
        }
    }

    // ==========================================
    // TIMER CONTROLS (High Accuracy System Clock)
    // ==========================================

    fun startTimer() {
        if (_timerState.value.isRunning) return

        val remaining = _timerState.value.remainingSeconds
        targetEndTimeMs = System.currentTimeMillis() + (remaining * 1000L)

        _timerState.value = _timerState.value.copy(
            isRunning = true,
            isPaused = false
        )

        // Ambient sound if enabled
        val settings = settingsFlow.value
        if (settings.soundEnabled) {
            soundSynthesizer.startAmbientSound(settings.ambientSound)
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(300)
                val now = System.currentTimeMillis()
                val diffMs = targetEndTimeMs - now
                val remainingSec = (diffMs / 1000L).coerceAtLeast(0L).toInt()

                _timerState.value = _timerState.value.copy(remainingSeconds = remainingSec)

                if (remainingSec <= 0) {
                    onSessionCompleted()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        soundSynthesizer.stopAmbientSound()

        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = true
        )
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        soundSynthesizer.stopAmbientSound()

        val settings = settingsFlow.value
        val durationMins = when (_timerState.value.mode) {
            SessionMode.FOCUS -> settings.focusDurationMinutes
            SessionMode.SHORT_BREAK -> settings.shortBreakMinutes
            SessionMode.LONG_BREAK -> settings.longBreakMinutes
        }
        val sec = durationMins * 60

        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            remainingSeconds = sec,
            totalDurationSeconds = sec
        )
    }

    fun skipSession() {
        timerJob?.cancel()
        timerJob = null
        soundSynthesizer.stopAmbientSound()
        advanceToNextMode(autoStart = false)
    }

    fun switchMode(mode: SessionMode) {
        timerJob?.cancel()
        timerJob = null
        soundSynthesizer.stopAmbientSound()

        val settings = settingsFlow.value
        val durationMins = when (mode) {
            SessionMode.FOCUS -> settings.focusDurationMinutes
            SessionMode.SHORT_BREAK -> settings.shortBreakMinutes
            SessionMode.LONG_BREAK -> settings.longBreakMinutes
        }
        val sec = durationMins * 60

        _timerState.value = _timerState.value.copy(
            mode = mode,
            isRunning = false,
            isPaused = false,
            remainingSeconds = sec,
            totalDurationSeconds = sec
        )
    }

    private fun onSessionCompleted() {
        soundSynthesizer.stopAmbientSound()
        val settings = settingsFlow.value
        if (settings.soundEnabled) {
            soundSynthesizer.playSessionCompleteSound()
        }
        if (settings.vibrationEnabled) {
            soundSynthesizer.triggerVibration()
        }

        val completedMode = _timerState.value.mode
        val activeTask = _timerState.value.activeTask
        val durationMins = _timerState.value.totalDurationSeconds / 60
        val strikes = _timerState.value.distractionStrikes

        // Record session in database
        viewModelScope.launch {
            val session = PomodoroSessionEntity(
                mode = completedMode,
                plannedDurationMinutes = durationMins,
                actualSecondsFocused = _timerState.value.totalDurationSeconds,
                taskId = activeTask?.id,
                taskTitle = activeTask?.title,
                completed = true,
                distractionStrikes = strikes
            )
            sessionDao.insertSession(session)

            // Update daily stats
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existingStat = dailyStatDao.getStatForDate(todayStr)
            if (existingStat != null) {
                if (completedMode == SessionMode.FOCUS) {
                    dailyStatDao.addFocusSession(todayStr, durationMins)
                }
            } else {
                dailyStatDao.insertOrUpdate(
                    DailyStatEntity(
                        dateString = todayStr,
                        totalFocusMinutes = if (completedMode == SessionMode.FOCUS) durationMins else 0,
                        completedSessions = if (completedMode == SessionMode.FOCUS) 1 else 0
                    )
                )
            }

            // Increment task completed pomodoros
            if (completedMode == SessionMode.FOCUS && activeTask != null) {
                taskDao.incrementPomodoro(activeTask.id)
                val updatedTask = taskDao.getTaskById(activeTask.id)
                _timerState.value = _timerState.value.copy(activeTask = updatedTask)
            }
        }

        // Advance cycle
        val autoStart = when (completedMode) {
            SessionMode.FOCUS -> settings.autoStartBreaks
            SessionMode.SHORT_BREAK, SessionMode.LONG_BREAK -> settings.autoStartPomodoro
        }
        advanceToNextMode(autoStart = autoStart)
    }

    private fun advanceToNextMode(autoStart: Boolean) {
        val currentMode = _timerState.value.mode
        val settings = settingsFlow.value
        var cycles = _timerState.value.completedCycles

        val nextMode: SessionMode
        if (currentMode == SessionMode.FOCUS) {
            cycles++
            if (cycles >= settings.longBreakInterval) {
                nextMode = SessionMode.LONG_BREAK
                cycles = 0
            } else {
                nextMode = SessionMode.SHORT_BREAK
            }
        } else {
            nextMode = SessionMode.FOCUS
        }

        val durationMins = when (nextMode) {
            SessionMode.FOCUS -> settings.focusDurationMinutes
            SessionMode.SHORT_BREAK -> settings.shortBreakMinutes
            SessionMode.LONG_BREAK -> settings.longBreakMinutes
        }
        val sec = durationMins * 60

        _timerState.value = _timerState.value.copy(
            mode = nextMode,
            completedCycles = cycles,
            isRunning = false,
            isPaused = false,
            remainingSeconds = sec,
            totalDurationSeconds = sec,
            distractionStrikes = 0
        )

        if (autoStart) {
            startTimer()
        }
    }

    fun setActiveTask(task: TaskItemEntity?) {
        _timerState.value = _timerState.value.copy(activeTask = task)
    }

    // ==========================================
    // DISTRACTION BLOCKER & APP SHIELD
    // ==========================================

    fun onAppPausedOrDistractionTriggered(targetAppName: String? = null) {
        if (_timerState.value.isRunning && _timerState.value.mode == SessionMode.FOCUS) {
            val settings = settingsFlow.value
            if (settings.appBlockerEnabled) {
                val newStrikes = _timerState.value.distractionStrikes + 1
                _timerState.value = _timerState.value.copy(
                    distractionStrikes = newStrikes,
                    showDistractionShieldDialog = true,
                    shieldTriggeredAppName = targetAppName ?: "Social / Games"
                )

                if (settings.vibrationEnabled) {
                    soundSynthesizer.triggerVibration(longArrayOf(0, 150, 100, 150))
                }

                // Record distraction avoided in daily stats
                viewModelScope.launch {
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val stat = dailyStatDao.getStatForDate(todayStr)
                    if (stat != null) {
                        dailyStatDao.incrementDistractionsResisted(todayStr)
                    } else {
                        dailyStatDao.insertOrUpdate(
                            DailyStatEntity(
                                dateString = todayStr,
                                distractionAttemptsResisted = 1
                            )
                        )
                    }
                }
            }
        }
    }

    fun dismissDistractionShield() {
        _timerState.value = _timerState.value.copy(
            showDistractionShieldDialog = false,
            shieldTriggeredAppName = null
        )
    }

    fun toggleAppBlocked(packageName: String, currentBlocked: Boolean) {
        viewModelScope.launch {
            blockedAppDao.setBlockedStatus(packageName, !currentBlocked)
        }
    }

    fun addCustomBlockedApp(name: String, pkg: String, category: AppBlockCategory = AppBlockCategory.CUSTOM) {
        if (name.isBlank()) return
        val finalPkg = if (pkg.isBlank()) "custom." + name.lowercase().replace(" ", "") else pkg.trim()
        viewModelScope.launch {
            blockedAppDao.insertApp(
                BlockedAppEntity(
                    packageName = finalPkg,
                    appName = name.trim(),
                    category = category,
                    isBlocked = true,
                    iconColorHex = "#8B5CF6"
                )
            )
        }
    }

    fun deleteBlockedApp(app: BlockedAppEntity) {
        viewModelScope.launch {
            blockedAppDao.deleteApp(app)
        }
    }

    // ==========================================
    // TASK MANAGEMENT
    // ==========================================

    fun addTask(
        title: String,
        notes: String = "",
        priority: TaskPriority = TaskPriority.MEDIUM,
        category: TaskCategory = TaskCategory.WORK,
        estimatedPomodoros: Int = 1
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = TaskItemEntity(
                title = title.trim(),
                notes = notes.trim(),
                priority = priority,
                category = category,
                estimatedPomodoros = estimatedPomodoros.coerceAtLeast(1)
            )
            val id = taskDao.insertTask(task)
            // If active task is null, automatically set this new task
            if (_timerState.value.activeTask == null) {
                _timerState.value = _timerState.value.copy(activeTask = task.copy(id = id))
            }
        }
    }

    fun toggleTaskComplete(task: TaskItemEntity) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            taskDao.setCompleted(task.id, newCompleted, if (newCompleted) System.currentTimeMillis() else null)

            if (newCompleted) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val stat = dailyStatDao.getStatForDate(todayStr)
                if (stat != null) {
                    dailyStatDao.incrementTaskCompleted(todayStr)
                } else {
                    dailyStatDao.insertOrUpdate(
                        DailyStatEntity(
                            dateString = todayStr,
                            completedTasks = 1
                        )
                    )
                }
            }

            // Update active task if it was this one
            if (_timerState.value.activeTask?.id == task.id) {
                val updated = taskDao.getTaskById(task.id)
                _timerState.value = _timerState.value.copy(activeTask = updated)
            }
        }
    }

    fun deleteTask(task: TaskItemEntity) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
            if (_timerState.value.activeTask?.id == task.id) {
                _timerState.value = _timerState.value.copy(activeTask = null)
            }
        }
    }

    // ==========================================
    // AI PRODUCTIVITY COACH
    // ==========================================

    fun askAiCoach(prompt: String) {
        if (prompt.isBlank() || _isAiLoading.value) return
        _isAiLoading.value = true
        _aiResponseText.value = null

        viewModelScope.launch {
            val tasks = tasksFlow.value
            val stats = dailyStatsFlow.value
            val totalSeconds = sessionDao.getTotalSecondsFocused() ?: 0L
            val totalMinutes = (totalSeconds / 60).toInt()
            val completedSessions = sessionDao.getTotalFocusSessionsCount()
            val customKey = settingsFlow.value.customAiKey

            val advice = aiCoach.generateCoachingInsights(
                userPrompt = prompt,
                customApiKey = customKey,
                totalFocusMinutes = totalMinutes,
                completedSessions = completedSessions,
                tasks = tasks,
                recentStats = stats
            )

            _aiResponseText.value = advice
            _isAiLoading.value = false
        }
    }

    fun clearAiResponse() {
        _aiResponseText.value = null
    }

    // ==========================================
    // CLOUD SYNCHRONIZATION
    // ==========================================

    fun syncWithCloud() {
        if (_syncStatus.value == SyncStatus.SYNCING) return
        _syncStatus.value = SyncStatus.SYNCING
        _syncMessage.value = "Connecting to Aura Cloud..."

        viewModelScope.launch {
            val result = cloudSyncManager.performCloudSync()
            _syncStatus.value = if (result.success) SyncStatus.SYNCED else SyncStatus.OFFLINE
            _syncMessage.value = result.message
        }
    }

    fun exportBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val json = cloudSyncManager.exportDataJson()
            onExported(json)
        }
    }

    fun importBackup(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = cloudSyncManager.importDataJson(json)
            if (success) {
                syncWithCloud()
            }
            onComplete(success)
        }
    }

    // ==========================================
    // SETTINGS UPDATES
    // ==========================================

    fun updateSettings(newSettings: UserSettings) {
        preferencesManager.updateSettings(newSettings)
    }

    fun setCustomTimerDurations(
        focusMinutes: Int,
        shortBreakMinutes: Int,
        longBreakMinutes: Int,
        longBreakInterval: Int = settingsFlow.value.longBreakInterval
    ) {
        val validFocus = focusMinutes.coerceIn(1, 240)
        val validShort = shortBreakMinutes.coerceIn(1, 90)
        val validLong = longBreakMinutes.coerceIn(1, 120)
        val validInterval = longBreakInterval.coerceIn(1, 12)

        timerJob?.cancel()
        timerJob = null
        soundSynthesizer.stopAmbientSound()

        val updated = settingsFlow.value.copy(
            focusDurationMinutes = validFocus,
            shortBreakMinutes = validShort,
            longBreakMinutes = validLong,
            longBreakInterval = validInterval
        )
        preferencesManager.updateSettings(updated)

        val currentModeMins = when (_timerState.value.mode) {
            SessionMode.FOCUS -> validFocus
            SessionMode.SHORT_BREAK -> validShort
            SessionMode.LONG_BREAK -> validLong
        }
        val sec = currentModeMins * 60
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            remainingSeconds = sec,
            totalDurationSeconds = sec
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundSynthesizer.stopAmbientSound()
    }
}
