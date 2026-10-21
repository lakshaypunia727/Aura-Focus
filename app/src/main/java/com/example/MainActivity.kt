package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BlockerScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.AuraFocusTheme
import com.example.ui.theme.NightFilterTint
import com.example.ui.viewmodel.AuraFocusViewModel

enum class AuraScreenTab {
    TIMER,
    TASKS,
    BLOCKER,
    ANALYTICS,
    AI_COACH
}

class MainActivity : ComponentActivity() {

    private val viewModel: AuraFocusViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settingsFlow.collectAsState()
            val timerState by viewModel.timerState.collectAsState()

            var currentTab by remember { mutableStateOf(AuraScreenTab.TIMER) }
            var showSettingsDialog by remember { mutableStateOf(false) }

            // Lifecycle Observer to detect leaving app during active Focus session
            DisposableEffect(this) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        viewModel.onAppPausedOrDistractionTriggered("Social Apps / Games")
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            // Back button returns to Timer if in sub-screen
            BackHandler(enabled = currentTab != AuraScreenTab.TIMER) {
                currentTab = AuraScreenTab.TIMER
            }

            AuraFocusTheme(
                theme = settings.appTheme,
                darkModeSetting = settings.darkMode
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = when (currentTab) {
                                            AuraScreenTab.TIMER -> "AuraFocus"
                                            AuraScreenTab.TASKS -> "Custom Tasks"
                                            AuraScreenTab.BLOCKER -> "App Blocker Shield"
                                            AuraScreenTab.ANALYTICS -> "Weekly Reports"
                                            AuraScreenTab.AI_COACH -> "Productivity Coach"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = { showSettingsDialog = true },
                                        modifier = Modifier.testTag("top_bar_settings_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings"
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = Color.Transparent
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier.testTag("main_bottom_nav"),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == AuraScreenTab.TIMER,
                                    onClick = { currentTab = AuraScreenTab.TIMER },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == AuraScreenTab.TIMER) Icons.Default.Timer else Icons.Outlined.Timer,
                                            contentDescription = "Timer"
                                        )
                                    },
                                    label = { Text("Timer", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_item_timer")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AuraScreenTab.TASKS,
                                    onClick = { currentTab = AuraScreenTab.TASKS },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == AuraScreenTab.TASKS) Icons.AutoMirrored.Filled.ListAlt else Icons.AutoMirrored.Outlined.ListAlt,
                                            contentDescription = "Tasks"
                                        )
                                    },
                                    label = { Text("Tasks", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_item_tasks")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AuraScreenTab.BLOCKER,
                                    onClick = { currentTab = AuraScreenTab.BLOCKER },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == AuraScreenTab.BLOCKER) Icons.Default.Security else Icons.Outlined.Security,
                                            contentDescription = "Blocker"
                                        )
                                    },
                                    label = { Text("Blocker", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_item_blocker")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AuraScreenTab.ANALYTICS,
                                    onClick = { currentTab = AuraScreenTab.ANALYTICS },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == AuraScreenTab.ANALYTICS) Icons.Default.BarChart else Icons.Outlined.BarChart,
                                            contentDescription = "Reports"
                                        )
                                    },
                                    label = { Text("Reports", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_item_reports")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AuraScreenTab.AI_COACH,
                                    onClick = { currentTab = AuraScreenTab.AI_COACH },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == AuraScreenTab.AI_COACH) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                                            contentDescription = "AI Coach"
                                        )
                                    },
                                    label = { Text("AI Coach", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_item_ai_coach")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Crossfade(
                            targetState = currentTab,
                            label = "tabCrossfade",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) { tab ->
                            when (tab) {
                                AuraScreenTab.TIMER -> TimerScreen(
                                    viewModel = viewModel,
                                    onNavigateToTasks = { currentTab = AuraScreenTab.TASKS }
                                )
                                AuraScreenTab.TASKS -> TasksScreen(
                                    viewModel = viewModel,
                                    onNavigateToTimer = { currentTab = AuraScreenTab.TIMER }
                                )
                                AuraScreenTab.BLOCKER -> BlockerScreen(
                                    viewModel = viewModel
                                )
                                AuraScreenTab.ANALYTICS -> AnalyticsScreen(
                                    viewModel = viewModel
                                )
                                AuraScreenTab.AI_COACH -> AiCoachScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }

                    // Eye Comfort Night Tint Filter
                    if (settings.eyeComfortTint) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NightFilterTint)
                        )
                    }

                    // Settings Dialog
                    if (showSettingsDialog) {
                        SettingsDialog(
                            viewModel = viewModel,
                            onDismiss = { showSettingsDialog = false }
                        )
                    }
                }
            }
        }
    }
}
