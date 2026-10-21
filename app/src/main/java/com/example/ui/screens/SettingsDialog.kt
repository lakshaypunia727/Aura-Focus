package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cloud.SyncStatus
import com.example.data.model.AmbientSoundType
import com.example.data.model.AppThemeSetting
import com.example.data.model.DarkModeSetting
import com.example.ui.theme.NeonPrimaryDark
import com.example.ui.theme.OceanPrimaryDark
import com.example.ui.theme.SakuraPrimaryDark
import com.example.ui.theme.SunsetPrimaryDark
import com.example.ui.theme.ZenForestPrimaryDark
import com.example.ui.viewmodel.AuraFocusViewModel

@Composable
fun SettingsDialog(
    viewModel: AuraFocusViewModel,
    onDismiss: () -> Unit
) {
    val settings by viewModel.settingsFlow.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonText by remember { mutableStateOf("") }
    var isImportMode by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AuraFocus Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // 1. 5 AESTHETIC THEMES PICKER
                // ==========================================
                Text(
                    text = "THEME PALETTE (5 STYLES)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ThemeSwatch(
                        title = "Forest",
                        color = ZenForestPrimaryDark,
                        isSelected = settings.appTheme == AppThemeSetting.ZEN_FOREST,
                        onClick = { viewModel.updateSettings(settings.copy(appTheme = AppThemeSetting.ZEN_FOREST)) }
                    )
                    ThemeSwatch(
                        title = "Sunset",
                        color = SunsetPrimaryDark,
                        isSelected = settings.appTheme == AppThemeSetting.SUNSET_GLOW,
                        onClick = { viewModel.updateSettings(settings.copy(appTheme = AppThemeSetting.SUNSET_GLOW)) }
                    )
                    ThemeSwatch(
                        title = "Midnight",
                        color = NeonPrimaryDark,
                        isSelected = settings.appTheme == AppThemeSetting.MIDNIGHT_NEON,
                        onClick = { viewModel.updateSettings(settings.copy(appTheme = AppThemeSetting.MIDNIGHT_NEON)) }
                    )
                    ThemeSwatch(
                        title = "Sakura",
                        color = SakuraPrimaryDark,
                        isSelected = settings.appTheme == AppThemeSetting.SAKURA_ROSE,
                        onClick = { viewModel.updateSettings(settings.copy(appTheme = AppThemeSetting.SAKURA_ROSE)) }
                    )
                    ThemeSwatch(
                        title = "Ocean",
                        color = OceanPrimaryDark,
                        isSelected = settings.appTheme == AppThemeSetting.OCEAN_DEEP,
                        onClick = { viewModel.updateSettings(settings.copy(appTheme = AppThemeSetting.OCEAN_DEEP)) }
                    )
                }

                // ==========================================
                // 2. DARK MODE & EYE STRAIN REDUCTION
                // ==========================================
                Text(
                    text = "DISPLAY & EYE COMFORT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = settings.darkMode == DarkModeSetting.SYSTEM,
                        onClick = { viewModel.updateSettings(settings.copy(darkMode = DarkModeSetting.SYSTEM)) },
                        label = { Text("Auto", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = settings.darkMode == DarkModeSetting.LIGHT,
                        onClick = { viewModel.updateSettings(settings.copy(darkMode = DarkModeSetting.LIGHT)) },
                        label = { Text("Light", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = settings.darkMode == DarkModeSetting.DARK,
                        onClick = { viewModel.updateSettings(settings.copy(darkMode = DarkModeSetting.DARK)) },
                        label = { Text("Dark", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = settings.darkMode == DarkModeSetting.OLED_BLACK,
                        onClick = { viewModel.updateSettings(settings.copy(darkMode = DarkModeSetting.OLED_BLACK)) },
                        label = { Text("OLED Pure", fontSize = 11.sp) }
                    )
                }

                // Eye Comfort Night Tint Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Eye Comfort Night Tint",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Warm amber circadian filter to reduce blue light & late session eye strain",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Switch(
                        checked = settings.eyeComfortTint,
                        onCheckedChange = { isEnabled ->
                            viewModel.updateSettings(settings.copy(eyeComfortTint = isEnabled))
                        }
                    )
                }

                // ==========================================
                // 3. TIMER DURATIONS
                // ==========================================
                Text(
                    text = "TIMER DURATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )

                DurationSelectorRow(
                    label = "Focus Duration",
                    currentValue = settings.focusDurationMinutes,
                    options = listOf(10, 15, 20, 25, 30, 45, 50, 60, 90),
                    unit = "m",
                    onSelect = {
                        viewModel.setCustomTimerDurations(
                            focusMinutes = it,
                            shortBreakMinutes = settings.shortBreakMinutes,
                            longBreakMinutes = settings.longBreakMinutes,
                            longBreakInterval = settings.longBreakInterval
                        )
                    }
                )

                DurationSelectorRow(
                    label = "Short Break",
                    currentValue = settings.shortBreakMinutes,
                    options = listOf(2, 3, 5, 8, 10, 15),
                    unit = "m",
                    onSelect = {
                        viewModel.setCustomTimerDurations(
                            focusMinutes = settings.focusDurationMinutes,
                            shortBreakMinutes = it,
                            longBreakMinutes = settings.longBreakMinutes,
                            longBreakInterval = settings.longBreakInterval
                        )
                    }
                )

                DurationSelectorRow(
                    label = "Long Break",
                    currentValue = settings.longBreakMinutes,
                    options = listOf(10, 15, 20, 25, 30, 45),
                    unit = "m",
                    onSelect = {
                        viewModel.setCustomTimerDurations(
                            focusMinutes = settings.focusDurationMinutes,
                            shortBreakMinutes = settings.shortBreakMinutes,
                            longBreakMinutes = it,
                            longBreakInterval = settings.longBreakInterval
                        )
                    }
                )

                DurationSelectorRow(
                    label = "Long Break Every",
                    currentValue = settings.longBreakInterval,
                    options = listOf(2, 3, 4, 5, 6),
                    unit = " 🍅",
                    onSelect = {
                        viewModel.setCustomTimerDurations(
                            focusMinutes = settings.focusDurationMinutes,
                            shortBreakMinutes = settings.shortBreakMinutes,
                            longBreakMinutes = settings.longBreakMinutes,
                            longBreakInterval = it
                        )
                    }
                )

                // Auto Start Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Auto-start Breaks", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = settings.autoStartBreaks,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoStartBreaks = it)) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Auto-start Pomodoros", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = settings.autoStartPomodoro,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoStartPomodoro = it)) }
                    )
                }

                // ==========================================
                // 4. SOUND & HAPTICS
                // ==========================================
                Text(
                    text = "AUDIO & HAPTICS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AmbientSoundType.values().forEach { sound ->
                        FilterChip(
                            selected = settings.ambientSound == sound,
                            onClick = { viewModel.updateSettings(settings.copy(ambientSound = sound)) },
                            label = {
                                Text(
                                    text = when (sound) {
                                        AmbientSoundType.MUTE -> "Mute"
                                        AmbientSoundType.ZEN_BELL -> "Zen Bell"
                                        AmbientSoundType.CLOCK_TICK -> "Tick"
                                        AmbientSoundType.RAIN_WHISPER -> "Rain"
                                        AmbientSoundType.WHITE_NOISE -> "Noise"
                                    },
                                    fontSize = 10.sp
                                )
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Vibration & Haptics", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(vibrationEnabled = it)) }
                    )
                }

                // ==========================================
                // 5. CLOUD SYNC & MULTI-DEVICE
                // ==========================================
                Text(
                    text = "CLOUD SYNC & BACKUP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Device Sync ID",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = settings.cloudSyncKey,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            ElevatedButton(
                                onClick = { viewModel.syncWithCloud() },
                                enabled = syncStatus != SyncStatus.SYNCING
                            ) {
                                if (syncStatus == SyncStatus.SYNCING) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync")
                                }
                            }
                        }

                        if (!syncMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = syncMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.exportBackup { json ->
                                        backupJsonText = json
                                        isImportMode = false
                                        showBackupDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export JSON", fontSize = 11.sp)
                            }

                            TextButton(
                                onClick = {
                                    backupJsonText = ""
                                    isImportMode = true
                                    showBackupDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import JSON", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            ElevatedButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )

    // Backup Export/Import Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text(if (isImportMode) "Import AuraFocus Backup" else "Export AuraFocus Backup") },
            text = {
                Column {
                    Text(
                        text = if (isImportMode) "Paste JSON backup data from another device to restore:" else "Copy this JSON backup to restore across other devices:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupJsonText,
                        onValueChange = { if (isImportMode) backupJsonText = it },
                        readOnly = !isImportMode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                if (isImportMode) {
                    ElevatedButton(
                        onClick = {
                            viewModel.importBackup(backupJsonText) { success ->
                                showBackupDialog = false
                            }
                        },
                        enabled = backupJsonText.isNotBlank()
                    ) {
                        Text("Restore Data")
                    }
                } else {
                    ElevatedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(backupJsonText))
                            showBackupDialog = false
                        }
                    ) {
                        Text("Copy to Clipboard")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ThemeSwatch(
    title: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun DurationSelectorRow(
    label: String,
    currentValue: Int,
    options: List<Int>,
    unit: String,
    onSelect: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: $currentValue$unit",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (currentValue > 1) onSelect(currentValue - 1) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease $label",
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { onSelect(currentValue + 1) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase $label",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { opt ->
                FilterChip(
                    selected = currentValue == opt,
                    onClick = { onSelect(opt) },
                    label = { Text("$opt$unit", fontSize = 11.sp) }
                )
            }
        }
    }
}
