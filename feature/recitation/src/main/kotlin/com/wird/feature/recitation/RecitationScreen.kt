package com.wird.feature.recitation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wird.core.ui.component.FeaturePlaceholder

@Composable
fun RecitationScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        title = "Recitation",
        subtitle = "Mistake detection via forced alignment against the known ayah text (R&D).",
        modifier = modifier,
    )
}
