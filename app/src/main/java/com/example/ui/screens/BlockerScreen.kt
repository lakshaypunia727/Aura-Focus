package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppBlockCategory
import com.example.data.model.BlockedAppEntity
import com.example.ui.viewmodel.AuraFocusViewModel

@Composable
fun BlockerScreen(
    viewModel: AuraFocusViewModel,
    modifier: Modifier = Modifier
) {
    val blockedApps by viewModel.blockedAppsFlow.collectAsState()
    val settings by viewModel.settingsFlow.collectAsState()
    val dailyStats by viewModel.dailyStatsFlow.collectAsState()

    var showAddAppDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<AppBlockCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val totalBlockedCount = blockedApps.count { it.isBlocked }
    val totalResisted = dailyStats.sumOf { it.distractionAttemptsResisted }

    val filteredApps = blockedApps.filter { app ->
        (selectedCategory == null || app.category == selectedCategory) &&
        (searchQuery.isBlank() || app.appName.contains(searchQuery, ignoreCase = true) || app.packageName.contains(searchQuery, ignoreCase = true))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Master Shield Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Shield",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Focus App Blocker",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (settings.appBlockerEnabled) "$totalBlockedCount apps guarded during sessions" else "Blocker is currently disabled",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = settings.appBlockerEnabled,
                            onCheckedChange = { isChecked ->
                                viewModel.updateSettings(settings.copy(appBlockerEnabled = isChecked))
                            },
                            modifier = Modifier.testTag("master_blocker_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Strict Mode & Test Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Strict",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Strict Lockout Mode",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Switch(
                            checked = settings.strictBlockerMode,
                            onCheckedChange = { isStrict ->
                                viewModel.updateSettings(settings.copy(strictBlockerMode = isStrict))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🛡️ Over $totalResisted distraction impulses shielded across your sessions",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Filter Categories
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search or filter apps...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedCategory == AppBlockCategory.SOCIAL,
                    onClick = { selectedCategory = AppBlockCategory.SOCIAL },
                    label = { Text("Social") }
                )
                FilterChip(
                    selected = selectedCategory == AppBlockCategory.MESSAGING,
                    onClick = { selectedCategory = AppBlockCategory.MESSAGING },
                    label = { Text("Chat") }
                )
                FilterChip(
                    selected = selectedCategory == AppBlockCategory.GAMES,
                    onClick = { selectedCategory = AppBlockCategory.GAMES },
                    label = { Text("Games") }
                )
                FilterChip(
                    selected = selectedCategory == AppBlockCategory.ENTERTAINMENT,
                    onClick = { selectedCategory = AppBlockCategory.ENTERTAINMENT },
                    label = { Text("Video") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    BlockedAppItem(
                        app = app,
                        onToggle = { viewModel.toggleAppBlocked(app.packageName, app.isBlocked) },
                        onDelete = { viewModel.deleteBlockedApp(app) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Add Custom App FAB
        FloatingActionButton(
            onClick = { showAddAppDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp)
                .testTag("add_custom_app_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Custom App")
        }

        // Add Custom App Dialog
        if (showAddAppDialog) {
            AddCustomAppDialog(
                onDismiss = { showAddAppDialog = false },
                onAddApp = { name, pkg, cat ->
                    viewModel.addCustomBlockedApp(name, pkg, cat)
                    showAddAppDialog = false
                }
            )
        }
    }
}

@Composable
fun BlockedAppItem(
    app: BlockedAppEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Avatar Color Dot
            val avatarColor = try {
                Color(android.graphics.Color.parseColor(app.iconColorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhoneAndroid,
                    contentDescription = app.appName,
                    tint = avatarColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "${app.category.name.lowercase().replaceFirstChar { it.uppercase() }} • ${app.packageName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }

            if (app.category == AppBlockCategory.CUSTOM) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("app_switch_${app.packageName}")
            )
        }
    }
}

@Composable
fun AddCustomAppDialog(
    onDismiss: () -> Unit,
    onAddApp: (String, String, AppBlockCategory) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(AppBlockCategory.CUSTOM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Block Custom App",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add any game, social app, or distraction to block during your focus sessions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name *") },
                    placeholder = { Text("e.g. Telegram, Candy Crush, Chess") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_app_name_input")
                )

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package (optional)") },
                    placeholder = { Text("e.g. org.telegram.messenger") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppBlockCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = {
                    if (appName.isNotBlank()) {
                        onAddApp(appName, packageName, category)
                    }
                },
                enabled = appName.isNotBlank()
            ) {
                Text("Add to Blocker")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
