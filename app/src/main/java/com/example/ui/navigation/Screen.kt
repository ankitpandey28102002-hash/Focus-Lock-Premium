package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Splash : Screen("splash", "FocusLock")
    object Onboarding : Screen("onboarding", "Welcome")
    object Dashboard : Screen("dashboard", "Dashboard")
    object AppSelector : Screen("app_selector", "Select Apps")
    object BlockListManager : Screen("block_lists", "Block Lists")
    object FocusModeSelector : Screen("focus_modes", "Focus Modes")
    object TimerSetup : Screen("timer_setup", "Start Focus")
    object ActiveSession : Screen("active_session", "Focusing")
    object StrictModeConfig : Screen("strict_mode", "Strict Mode")
    object SessionSummary : Screen("session_summary", "Session Complete")
    object ScheduleManager : Screen("schedules", "Schedules")
    object WeeklyCalendar : Screen("calendar", "Weekly Calendar")
    object Insights : Screen("insights", "Analytics")
    object History : Screen("history", "Session History")
    object ThemeSettings : Screen("themes", "Themes")
    object NotificationSettings : Screen("notifications", "Notifications")
    object Permissions : Screen("permissions", "Permissions & Status")
    object BackupRestore : Screen("backup_restore", "Backup & Restore")
    object Preferences : Screen("preferences", "Preferences")
    object DevTesting : Screen("dev_testing", "Developer Testing")
}
