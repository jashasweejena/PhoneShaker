package com.example.phoneshaker.activity.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun AppEntry(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        SettingsPage()
    }
}