package com.wird.feature.alarm

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wird.core.ui.component.FeaturePlaceholder

@Composable
fun AlarmScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        title = "Fajr Alarm",
        subtitle = "Dismiss-proof exact alarm: full-screen intent, dismissal tasks, rising volume.",
        modifier = modifier,
    )
}
