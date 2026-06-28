package com.wird.feature.quran.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun JuzReaderRoute(
    onBack: () -> Unit,
    viewModel: JuzReaderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReaderScreen(uiState = uiState, onBack = onBack, onVisibleAyah = viewModel::onVisibleAyahChanged)
}
