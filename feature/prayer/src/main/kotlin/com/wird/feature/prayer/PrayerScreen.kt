package com.wird.feature.prayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wird.core.ui.component.FeaturePlaceholder

@Composable
fun PrayerScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        title = "Prayer",
        subtitle = "Prayer times, qada ledger, adhkar, auto-DND.",
        modifier = modifier,
    )
}
