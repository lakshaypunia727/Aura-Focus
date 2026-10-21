package com.example.cloud

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.BlockedAppEntity
import com.example.data.model.DailyStatEntity
import com.example.data.model.PomodoroSessionEntity
import com.example.data.model.TaskItemEntity
import com.example.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE
}

data class SyncResult(
    val success: Boolean,
    val itemsSynced: Int,
    val timestampFormatted: String,
    val message: String
)

class CloudSyncManager(
    private val context: Context,
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager
) {
    suspend fun performCloudSync(): SyncResult = withContext(Dispatchers.IO) {
        val tasks = database.taskDao().getAllTasksFlow().first()
        val sessions = database.sessionDao().getAllSessionsFlow().first()
        val blockedApps = database.blockedAppDao().getAllBlockedAppsFlow().first()
        val stats = database.dailyStatDao().getRecentDailyStatsFlow().first()

        // Simulate network handshake and secure cloud synchronization
        delay(900)

        val now = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date())
        val totalCount = tasks.size + sessions.size + blockedApps.size + stats.size

        val currentSettings = preferencesManager.settingsFlow.value
        preferencesManager.updateSettings(
            currentSettings.copy(
                lastSyncTimeFormatted = now
            )
        )

        SyncResult(
            success = true,
            itemsSynced = totalCount,
            timestampFormatted = now,
            message = "Successfully synchronized $totalCount records across devices."
        )
    }

    suspend fun exportDataJson(): String = withContext(Dispatchers.IO) {
        val tasks = database.taskDao().getAllTasksFlow().first()
        val sessions = database.sessionDao().getAllSessionsFlow().first()
        val blockedApps = database.blockedAppDao().getAllBlockedAppsFlow().first()
        val stats = database.dailyStatDao().getRecentDailyStatsFlow().first()
        val settings = preferencesManager.settingsFlow.value

        val root = JSONObject().apply {
            put("version", 1)
            put("syncKey", settings.cloudSyncKey)
            put("exportedAt", System.currentTimeMillis())

            val tasksArray = JSONArray()
            for (t in tasks) {
                tasksArray.put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("notes", t.notes)
                    put("priority", t.priority.name)
                    put("category", t.category.name)
                    put("estimatedPomodoros", t.estimatedPomodoros)
                    put("completedPomodoros", t.completedPomodoros)
                    put("isCompleted", t.isCompleted)
                })
            }
            put("tasks", tasksArray)

            val sessionsArray = JSONArray()
            for (s in sessions) {
                sessionsArray.put(JSONObject().apply {
                    put("id", s.id)
                    put("mode", s.mode.name)
                    put("plannedDurationMinutes", s.plannedDurationMinutes)
                    put("actualSecondsFocused", s.actualSecondsFocused)
                    put("taskTitle", s.taskTitle ?: "")
                    put("timestamp", s.timestamp)
                    put("completed", s.completed)
                })
            }
            put("sessions", sessionsArray)

            val blockedArray = JSONArray()
            for (b in blockedApps) {
                blockedArray.put(JSONObject().apply {
                    put("packageName", b.packageName)
                    put("appName", b.appName)
                    put("isBlocked", b.isBlocked)
                    put("category", b.category.name)
                })
            }
            put("blockedApps", blockedArray)
        }

        root.toString(2)
    }

    suspend fun importDataJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val tasksArray = root.optJSONArray("tasks")
            if (tasksArray != null) {
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    database.taskDao().insertTask(
                        TaskItemEntity(
                            title = obj.getString("title"),
                            notes = obj.optString("notes", ""),
                            estimatedPomodoros = obj.optInt("estimatedPomodoros", 1),
                            completedPomodoros = obj.optInt("completedPomodoros", 0),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
            }
            val syncKey = root.optString("syncKey", "")
            if (syncKey.isNotBlank()) {
                val current = preferencesManager.settingsFlow.value
                val now = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date())
                preferencesManager.updateSettings(
                    current.copy(
                        cloudSyncKey = syncKey,
                        lastSyncTimeFormatted = now
                    )
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
