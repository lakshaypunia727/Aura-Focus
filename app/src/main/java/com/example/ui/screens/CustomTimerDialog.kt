package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class TimerPreset(
    val name: String,
    val focusMins: Int,
    val shortBreakMins: Int,
    val longBreakMins: Int,
    val cycles: Int = 4
)

@Composable
fun CustomTimerDialog(
    initialFocusMinutes: Int,
    initialShortBreakMinutes: Int,
    initialLongBreakMinutes: Int,
    initialLongBreakInterval: Int,
    onDismiss: () -> Unit,
    onApplyCustomTimer: (focusMins: Int, shortBreakMins: Int, longBreakMins: Int, interval: Int) -> Unit
) {
    var focusMinutes by remember { mutableIntStateOf(initialFocusMinutes.coerceIn(1, 240)) }
    var focusText by remember { mutableStateOf(initialFocusMinutes.toString()) }

    var shortBreakMinutes by remember { mutableIntStateOf(initialShortBreakMinutes.coerceIn(1, 90)) }
    var shortBreakText by remember { mutableStateOf(initialShortBreakMinutes.toString()) }

    var longBreakMinutes by remember { mutableIntStateOf(initialLongBreakMinutes.coerceIn(1, 120)) }
    var longBreakText by remember { mutableStateOf(initialLongBreakMinutes.toString()) }

    var longBreakInterval by remember { mutableIntStateOf(initialLongBreakInterval.coerceIn(1, 12)) }

    val presets = listOf(
        TimerPreset("Classic 25/5", 25, 5, 15, 4),
        TimerPreset("Deep 50/10", 50, 10, 25, 4),
        TimerPreset("Study 45/10", 45, 10, 20, 4),
        TimerPreset("Sprint 15/3", 15, 3, 10, 4),
        TimerPreset("Ultradian 90/20", 90, 20, 30, 3)
    )

    fun updateFocus(newVal: Int) {
        val clamped = newVal.coerceIn(1, 240)
        focusMinutes = clamped
        focusText = clamped.toString()
    }

    fun updateShortBreak(newVal: Int) {
        val clamped = newVal.coerceIn(1, 90)
        shortBreakMinutes = clamped
        shortBreakText = clamped.toString()
    }

    fun updateLongBreak(newVal: Int) {
        val clamped = newVal.coerceIn(1, 120)
        longBreakMinutes = clamped
        longBreakText = clamped.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Custom Timer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Set Custom Timer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Customize your focus & break durations",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Presets Row
                Text(
                    text = "QUICK PRESETS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = focusMinutes == preset.focusMins &&
                            shortBreakMinutes == preset.shortBreakMins &&
                            longBreakMinutes == preset.longBreakMins
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                updateFocus(preset.focusMins)
                                updateShortBreak(preset.shortBreakMins)
                                updateLongBreak(preset.longBreakMins)
                                longBreakInterval = preset.cycles
                            },
                            label = {
                                Text(
                                    text = preset.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // 1. Focus Duration Customizer
                CustomDurationCard(
                    title = "Focus Session",
                    subtitle = "Deep work interval",
                    minutes = focusMinutes,
                    textValue = focusText,
                    sliderRange = 1f..120f,
                    inputTestTag = "custom_focus_minutes_input",
                    onTextChanged = { text ->
                        focusText = text.filter { it.isDigit() }.take(3)
                        focusText.toIntOrNull()?.let {
                            if (it in 1..240) focusMinutes = it
                        }
                    },
                    onStepChange = { delta -> updateFocus(focusMinutes + delta) },
                    onSliderChange = { value -> updateFocus(value.roundToInt()) }
                )

                // 2. Short Break Customizer
                CustomDurationCard(
                    title = "Short Break",
                    subtitle = "Quick breather between cycles",
                    minutes = shortBreakMinutes,
                    textValue = shortBreakText,
                    sliderRange = 1f..45f,
                    inputTestTag = "custom_short_break_input",
                    onTextChanged = { text ->
                        shortBreakText = text.filter { it.isDigit() }.take(2)
                        shortBreakText.toIntOrNull()?.let {
                            if (it in 1..90) shortBreakMinutes = it
                        }
                    },
                    onStepChange = { delta -> updateShortBreak(shortBreakMinutes + delta) },
                    onSliderChange = { value -> updateShortBreak(value.roundToInt()) }
                )

                // 3. Long Break Customizer
                CustomDurationCard(
                    title = "Long Break",
                    subtitle = "Extended recovery after cycle set",
                    minutes = longBreakMinutes,
                    textValue = longBreakText,
                    sliderRange = 1f..60f,
                    inputTestTag = "custom_long_break_input",
                    onTextChanged = { text ->
                        longBreakText = text.filter { it.isDigit() }.take(3)
                        longBreakText.toIntOrNull()?.let {
                            if (it in 1..120) longBreakMinutes = it
                        }
                    },
                    onStepChange = { delta -> updateLongBreak(longBreakMinutes + delta) },
                    onSliderChange = { value -> updateLongBreak(value.roundToInt()) }
                )

                // 4. Cycles before Long Break
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Long Break Interval",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Every $longBreakInterval focus sessions (🍅)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { if (longBreakInterval > 1) longBreakInterval-- },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease cycles",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$longBreakInterval",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.Center
                            )

                            FilledTonalIconButton(
                                onClick = { if (longBreakInterval < 12) longBreakInterval++ },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase cycles",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = {
                    val finalFocus = focusText.toIntOrNull()?.coerceIn(1, 240) ?: focusMinutes
                    val finalShort = shortBreakText.toIntOrNull()?.coerceIn(1, 90) ?: shortBreakMinutes
                    val finalLong = longBreakText.toIntOrNull()?.coerceIn(1, 120) ?: longBreakMinutes
                    onApplyCustomTimer(finalFocus, finalShort, finalLong, longBreakInterval)
                },
                modifier = Modifier.testTag("apply_custom_timer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save & Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CustomDurationCard(
    title: String,
    subtitle: String,
    minutes: Int,
    textValue: String,
    sliderRange: ClosedFloatingPointRange<Float>,
    inputTestTag: String,
    onTextChanged: (String) -> Unit,
    onStepChange: (Int) -> Unit,
    onSliderChange: (Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                // Direct Minute Input Field
                OutlinedTextField(
                    value = textValue,
                    onValueChange = onTextChanged,
                    singleLine = true,
                    suffix = { Text("m", style = MaterialTheme.typography.labelSmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(86.dp)
                        .testTag(inputTestTag)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slider
            Slider(
                value = minutes.toFloat().coerceIn(sliderRange.start, sliderRange.endInclusive),
                onValueChange = onSliderChange,
                valueRange = sliderRange,
                modifier = Modifier.fillMaxWidth()
            )

            // Quick Stepper Pills (-5m, -1m, +1m, +5m)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(-5 to "-5m", -1 to "-1m", 1 to "+1m", 5 to "+5m").forEach { (delta, label) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onStepChange(delta) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
