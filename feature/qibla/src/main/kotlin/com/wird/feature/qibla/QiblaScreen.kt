package com.wird.feature.qibla

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wird.core.ui.component.FeaturePlaceholder

@Composable
fun QiblaScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        title = "Qibla",
        subtitle = "Sensor-fusion compass with declination correction; AR mode later.",
        modifier = modifier,
    )
}
