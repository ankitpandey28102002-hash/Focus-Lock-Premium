package com.example.data.model

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val category: String = "App",
    val isSelected: Boolean = false
)
