package com.wird.feature.quran

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wird.core.ui.component.FeaturePlaceholder

@Composable
fun QuranScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        title = "Quran",
        subtitle = "Reader: surah/juz navigation, EN/BN translation toggle, resume position.",
        modifier = modifier,
    )
}
