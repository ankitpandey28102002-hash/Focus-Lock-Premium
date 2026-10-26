package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.viewmodel.*

@Composable
fun FocusLockAppNavigation(
    focusViewModel: FocusViewModel,
    blockListViewModel: BlockListViewModel,
    scheduleViewModel: ScheduleViewModel,
    analyticsViewModel: AnalyticsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val isOnboardingDone by settingsViewModel.appContextIsOnboardingDone()
    val activeSessionState by focusViewModel.serviceSessionState.collectAsStateWithLifecycle()

    val backStack = remember { mutableStateListOf<Screen>(Screen.Splash) }
    val currentScreen = backStack.lastOrNull() ?: Screen.Dashboard

    fun navigateTo(screen: Screen) {
        if (screen == Screen.Dashboard) {
            backStack.clear()
            backStack.add(Screen.Dashboard)
        } else {
            backStack.add(screen)
        }
    }

    fun navigateToRoute(route: String) {
        val target = when (route) {
            Screen.Splash.route -> Screen.Splash
            Screen.Onboarding.route -> Screen.Onboarding
            Screen.Dashboard.route -> Screen.Dashboard
            Screen.AppSelector.route -> Screen.AppSelector
            Screen.BlockListManager.route -> Screen.BlockListManager
            Screen.FocusModeSelector.route -> Screen.FocusModeSelector
            Screen.TimerSetup.route -> Screen.TimerSetup
            Screen.ActiveSession.route -> Screen.ActiveSession
            Screen.StrictModeConfig.route -> Screen.StrictModeConfig
            Screen.SessionSummary.route -> Screen.SessionSummary
            Screen.ScheduleManager.route -> Screen.ScheduleManager
            Screen.WeeklyCalendar.route -> Screen.WeeklyCalendar
            Screen.Insights.route -> Screen.Insights
            Screen.History.route -> Screen.History
            Screen.ThemeSettings.route -> Screen.ThemeSettings
            Screen.NotificationSettings.route -> Screen.NotificationSettings
            Screen.Permissions.route -> Screen.Permissions
            Screen.BackupRestore.route -> Screen.BackupRestore
            Screen.Preferences.route -> Screen.Preferences
            Screen.DevTesting.route -> Screen.DevTesting
            else -> Screen.Dashboard
        }
        navigateTo(target)
    }

    fun popBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    // Hardware back handler
    BackHandler(enabled = backStack.size > 1 && currentScreen !is Screen.Dashboard) {
        popBack()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "ScreenTransition",
        modifier = Modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            is Screen.Splash -> {
                SplashScreen(
                    onTimeout = {
                        backStack.clear()
                        if (isOnboardingDone) {
                            backStack.add(Screen.Dashboard)
                        } else {
                            backStack.add(Screen.Onboarding)
                        }
                    }
                )
            }
            is Screen.Onboarding -> {
                OnboardingScreen(
                    onComplete = {
                        settingsViewModel.completeOnboarding()
                        backStack.clear()
                        backStack.add(Screen.Dashboard)
                    }
                )
            }
            is Screen.Dashboard -> {
                DashboardScreen(
                    focusViewModel = focusViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigate = { navigateToRoute(it) }
                )
            }
            is Screen.AppSelector -> {
                AppSelectorScreen(
                    blockListViewModel = blockListViewModel,
                    onDone = { popBack() }
                )
            }
            is Screen.BlockListManager -> {
                BlockListManagerScreen(
                    blockListViewModel = blockListViewModel,
                    onNavigate = { navigateToRoute(it) },
                    onBack = { popBack() }
                )
            }
            is Screen.FocusModeSelector -> {
                FocusModeSelectorScreen(
                    focusViewModel = focusViewModel,
                    onNavigate = { navigateToRoute(it) },
                    onBack = { popBack() }
                )
            }
            is Screen.TimerSetup -> {
                TimerSetupScreen(
                    focusViewModel = focusViewModel,
                    onNavigate = { navigateToRoute(it) },
                    onBack = { popBack() }
                )
            }
            is Screen.ActiveSession -> {
                ActiveSessionScreen(
                    focusViewModel = focusViewModel,
                    onNavigate = { navigateToRoute(it) },
                    onBackToDashboard = {
                        backStack.clear()
                        backStack.add(Screen.Dashboard)
                    }
                )
            }
            is Screen.StrictModeConfig -> {
                StrictModeConfigScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { popBack() }
                )
            }
            is Screen.SessionSummary -> {
                SessionSummaryScreen(
                    focusViewModel = focusViewModel,
                    onDone = {
                        backStack.clear()
                        backStack.add(Screen.Dashboard)
                    }
                )
            }
            is Screen.ScheduleManager -> {
                ScheduleManagerScreen(
                    scheduleViewModel = scheduleViewModel,
                    onNavigate = { navigateToRoute(it) },
                    onBack = { popBack() }
                )
            }
            is Screen.WeeklyCalendar -> {
                WeeklyCalendarScreen(
                    scheduleViewModel = scheduleViewModel,
                    onBack = { popBack() }
                )
            }
            is Screen.Insights -> {
                InsightsScreen(
                    analyticsViewModel = analyticsViewModel,
                    onBack = { popBack() }
                )
            }
            is Screen.History -> {
                HistoryScreen(
                    analyticsViewModel = analyticsViewModel,
                    focusViewModel = focusViewModel,
                    onBack = { popBack() }
                )
            }
            is Screen.ThemeSettings -> {
                ThemeSettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { popBack() }
                )
            }
            is Screen.NotificationSettings -> {
                NotificationSettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { popBack() }
                )
            }
            is Screen.Permissions -> {
                PermissionsScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { popBack() }
                )
            }
            is Screen.BackupRestore -> {
                BackupRestoreScreen(
                    analyticsViewModel = analyticsViewModel,
                    settingsViewModel = settingsViewModel,
                    onBack = { popBack() }
                )
            }
            is Screen.Preferences -> {
                ProfilePreferencesScreen(
                    settingsViewModel = settingsViewModel,
                    onNavigate = { navigateToRoute(it) },
                    onBack = { popBack() }
                )
            }
            is Screen.DevTesting -> {
                DeveloperTestScreen(
                    focusViewModel = focusViewModel,
                    settingsViewModel = settingsViewModel,
                    analyticsViewModel = analyticsViewModel,
                    scheduleViewModel = scheduleViewModel,
                    onBack = { popBack() }
                )
            }
        }
    }
}
