package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmbientSoundType
import com.example.data.model.SessionMode
import com.example.data.model.TaskItemEntity
import com.example.ui.viewmodel.AuraFocusViewModel
import java.util.Locale

@Composable
fun TimerScreen(
    viewModel: AuraFocusViewModel,
    onNavigateToTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerState by viewModel.timerState.collectAsState()
    val settings by viewModel.settingsFlow.collectAsState()
    val tasks by viewModel.tasksFlow.collectAsState()
    val blockedApps by viewModel.blockedAppsFlow.collectAsState()

    var showTaskPicker by remember { mutableStateOf(false) }
    var showCustomTimerDialog by remember { mutableStateOf(false) }

    val activeBlockedCount = blockedApps.count { it.isBlocked }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Mode Selector Bar (Focus, Short Break, Long Break)
            ModeSelectorPill(
                currentMode = timerState.mode,
                focusMinutes = settings.focusDurationMinutes,
                shortBreakMinutes = settings.shortBreakMinutes,
                longBreakMinutes = settings.longBreakMinutes,
                onModeSelected = { mode -> viewModel.switchMode(mode) },
                isRunning = timerState.isRunning
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Circular Aesthetic Hero Timer
            HeroTimerDisplay(
                remainingSeconds = timerState.remainingSeconds,
                totalSeconds = timerState.totalDurationSeconds,
                mode = timerState.mode,
                isRunning = timerState.isRunning,
                onCustomizeTimerClick = { showCustomTimerDialog = true }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Cycle Dots + Set Custom Timer Pill Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PomodoroCycleIndicator(
                    completedCycles = timerState.completedCycles,
                    maxCycles = settings.longBreakInterval,
                    currentMode = timerState.mode
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showCustomTimerDialog = true }
                        .testTag("set_custom_timer_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Set Custom Timer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Set Timer",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Play / Pause / Reset / Skip Controls
            TimerControls(
                isRunning = timerState.isRunning,
                isPaused = timerState.isPaused,
                onStart = { viewModel.startTimer() },
                onPause = { viewModel.pauseTimer() },
                onReset = { viewModel.resetTimer() },
                onSkip = { viewModel.skipSession() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Active Task Card
            ActiveTaskCard(
                activeTask = timerState.activeTask,
                onChangeTaskClick = { showTaskPicker = true },
                onToggleComplete = { task -> viewModel.toggleTaskComplete(task) },
                onGoToTasks = onNavigateToTasks
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Distraction Shield & Ambient Sound Info Bar
            DistractionAndSoundStatus(
                blockerEnabled = settings.appBlockerEnabled,
                blockedCount = activeBlockedCount,
                ambientSound = settings.ambientSound,
                onTriggerShieldTest = { viewModel.onAppPausedOrDistractionTriggered("WhatsApp") }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Distraction Shield Alert Dialog
        if (timerState.showDistractionShieldDialog) {
            DistractionShieldDialog(
                appName = timerState.shieldTriggeredAppName ?: "Distracting Apps",
                strikeCount = timerState.distractionStrikes,
                onReturnToFocus = { viewModel.dismissDistractionShield() }
            )
        }

        // Quick Task Picker Dialog
        if (showTaskPicker) {
            TaskPickerDialog(
                tasks = tasks,
                currentTask = timerState.activeTask,
                onSelectTask = { task ->
                    viewModel.setActiveTask(task)
                    showTaskPicker = false
                },
                onDismiss = { showTaskPicker = false },
                onGoToTasks = {
                    showTaskPicker = false
                    onNavigateToTasks()
                }
            )
        }

        // Custom Timer Studio Dialog
        if (showCustomTimerDialog) {
            CustomTimerDialog(
                initialFocusMinutes = settings.focusDurationMinutes,
                initialShortBreakMinutes = settings.shortBreakMinutes,
                initialLongBreakMinutes = settings.longBreakMinutes,
                initialLongBreakInterval = settings.longBreakInterval,
                onDismiss = { showCustomTimerDialog = false },
                onApplyCustomTimer = { focusMins, shortBreakMins, longBreakMins, interval ->
                    viewModel.setCustomTimerDurations(focusMins, shortBreakMins, longBreakMins, interval)
                    showCustomTimerDialog = false
                }
            )
        }
    }
}

@Composable
fun ModeSelectorPill(
    currentMode: SessionMode,
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    onModeSelected: (SessionMode) -> Unit,
    isRunning: Boolean
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeOption(
                label = "Focus (${focusMinutes}m)",
                selected = currentMode == SessionMode.FOCUS,
                onClick = { onModeSelected(SessionMode.FOCUS) }
            )
            Spacer(modifier = Modifier.width(4.dp))
            ModeOption(
                label = "Short (${shortBreakMinutes}m)",
                selected = currentMode == SessionMode.SHORT_BREAK,
                onClick = { onModeSelected(SessionMode.SHORT_BREAK) }
            )
            Spacer(modifier = Modifier.width(4.dp))
            ModeOption(
                label = "Long (${longBreakMinutes}m)",
                selected = currentMode == SessionMode.LONG_BREAK,
                onClick = { onModeSelected(SessionMode.LONG_BREAK) }
            )
        }
    }
}

@Composable
fun ModeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun HeroTimerDisplay(
    remainingSeconds: Int,
    totalSeconds: Int,
    mode: SessionMode,
    isRunning: Boolean,
    onCustomizeTimerClick: () -> Unit = {}
) {
    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "timerProgress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val modeTitle = when (mode) {
        SessionMode.FOCUS -> "Deep Focus"
        SessionMode.SHORT_BREAK -> "Rest & Breathe"
        SessionMode.LONG_BREAK -> "Extended Recovery"
    }

    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(CircleShape)
            .clickable(onClick = onCustomizeTimerClick)
            .testTag("circular_timer_container"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Animated Gradient Progress Arc
            val brush = Brush.sweepGradient(
                colors = listOf(primaryColor, secondaryColor, primaryColor),
                center = center
            )
            drawArc(
                brush = brush,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inner Timer Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = modeTitle.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.testTag("timer_time_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isRunning) "Flow in progress..." else "Tap to set custom time",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            )
        }
    }
}

@Composable
fun PomodoroCycleIndicator(
    completedCycles: Int,
    maxCycles: Int,
    currentMode: SessionMode
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until maxCycles) {
            val isCompleted = i < completedCycles
            val isCurrent = i == completedCycles && currentMode == SessionMode.FOCUS

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 12.dp else 10.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Cycle $completedCycles of $maxCycles",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun TimerControls(
    isRunning: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset Button
        FilledTonalIconButton(
            onClick = onReset,
            modifier = Modifier
                .size(52.dp)
                .testTag("timer_reset_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Timer",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Large Primary Play/Pause Button
        ElevatedButton(
            onClick = { if (isRunning) onPause() else onStart() },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .height(64.dp)
                .width(140.dp)
                .testTag("timer_play_pause_button"),
            colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "PAUSE" else if (isPaused) "RESUME" else "START",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Skip Button
        FilledTonalIconButton(
            onClick = onSkip,
            modifier = Modifier
                .size(52.dp)
                .testTag("timer_skip_button")
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Skip Session",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActiveTaskCard(
    activeTask: TaskItemEntity?,
    onChangeTaskClick: () -> Unit,
    onToggleComplete: (TaskItemEntity) -> Unit,
    onGoToTasks: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_task_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.List,
                        contentDescription = "Task",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FOCUSED TASK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                TextButton(onClick = onChangeTaskClick) {
                    Text(
                        text = if (activeTask != null) "Change" else "Select",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (activeTask != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onToggleComplete(activeTask) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (activeTask.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                            contentDescription = "Toggle Complete",
                            tint = if (activeTask.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeTask.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (activeTask.notes.isNotBlank()) {
                            Text(
                                text = activeTask.notes,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "🍅 ${activeTask.completedPomodoros}/${activeTask.estimatedPomodoros}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onChangeTaskClick)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No task selected. Tap to attach a task for this session.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DistractionAndSoundStatus(
    blockerEnabled: Boolean,
    blockedCount: Int,
    ambientSound: AmbientSoundType,
    onTriggerShieldTest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Distraction Shield status
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onTriggerShieldTest)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Shield",
                    tint = if (blockerEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (blockerEnabled) "Shield Active" else "Shield Off",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (blockerEnabled) "$blockedCount apps guarded" else "Tap to toggle",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Ambient Sound status
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Sound",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = when (ambientSound) {
                            AmbientSoundType.ZEN_BELL -> "Zen Bells"
                            AmbientSoundType.CLOCK_TICK -> "Clock Tick"
                            AmbientSoundType.RAIN_WHISPER -> "Rain Whisper"
                            AmbientSoundType.WHITE_NOISE -> "White Noise"
                            AmbientSoundType.MUTE -> "Muted"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Audio ambiance",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DistractionShieldDialog(
    appName: String,
    strikeCount: Int,
    onReturnToFocus: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onReturnToFocus,
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Shield Guard",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Distraction Shielded",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$appName is restricted during active Deep Focus.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "“The ability to perform deep work is becoming increasingly rare at exactly the same time it is becoming increasingly valuable.”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
                if (strikeCount > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚡ Distraction resistance strikes: $strikeCount",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = onReturnToFocus,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Return to Focus")
            }
        }
    )
}

@Composable
fun TaskPickerDialog(
    tasks: List<TaskItemEntity>,
    currentTask: TaskItemEntity?,
    onSelectTask: (TaskItemEntity?) -> Unit,
    onDismiss: () -> Unit,
    onGoToTasks: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attach Task to Timer") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (tasks.isEmpty()) {
                    Text(
                        text = "No tasks yet. Create your first task to track pomodoros!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Option to unassign
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (currentTask == null) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTask(null) }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "— None (General Focus) —",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    tasks.filter { !it.isCompleted }.forEach { task ->
                        val isSelected = currentTask?.id == task.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTask(task) }
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "🍅 ${task.completedPomodoros}/${task.estimatedPomodoros}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onGoToTasks) {
                Text("Manage Tasks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
