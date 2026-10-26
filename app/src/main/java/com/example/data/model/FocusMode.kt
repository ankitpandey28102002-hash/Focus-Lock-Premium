package com.example.data.model

enum class FocusMode(
    val title: String,
    val subtitle: String,
    val defaultDurationMinutes: Int,
    val isStrictByDefault: Boolean,
    val iconName: String
) {
    QUICK("Quick Focus", "Fast bursts for rapid productivity", 15, false, "FlashOn"),
    POMODORO("Pomodoro", "25m focus + 5m rest intervals", 25, false, "Timer"),
    DEEP_FOCUS("Deep Focus", "Uninterrupted high-intensity flow", 45, false, "Psychology"),
    STRICT("Strict Lock", "Zero interruption, unbreakable shield", 30, true, "Lock"),
    STUDY("Study Session", "Designed for deep reading & research", 50, false, "School"),
    WORK("Work Sprint", "Block distracting socials & games", 60, false, "Work"),
    SLEEP("Sleep & Wind Down", "Quiet your device for restorative sleep", 480, true, "Bedtime"),
    CUSTOM("Custom Profile", "Tailor duration, breaks & block lists", 30, false, "Tune")
}
