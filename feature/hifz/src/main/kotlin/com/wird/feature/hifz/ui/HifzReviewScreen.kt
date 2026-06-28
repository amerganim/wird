package com.wird.feature.hifz.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.core.ui.theme.ArabicAyahTextStyle
import com.wird.feature.hifz.data.Sm2

@Composable
fun HifzReviewRoute(
    onFinish: () -> Unit,
    viewModel: HifzReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HifzReviewScreen(
        state = state,
        onReveal = viewModel::reveal,
        onGrade = viewModel::grade,
        onFinish = onFinish,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HifzReviewScreen(
    state: ReviewUiState,
    onReveal: () -> Unit,
    onGrade: (Sm2.Grade) -> Unit,
    onFinish: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state) {
                            is ReviewUiState.Reviewing -> "Review ${state.position}/${state.total}"
                            else -> "Review"
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                ReviewUiState.Loading -> CircularProgressIndicator()

                ReviewUiState.Done -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("All done", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "You've reviewed everything due today.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = onFinish) { Text("Finish") }
                }

                is ReviewUiState.Reviewing -> ReviewingContent(state, onReveal, onGrade)
            }
        }
    }
}

@Composable
private fun ReviewingContent(
    state: ReviewUiState.Reviewing,
    onReveal: () -> Unit,
    onGrade: (Sm2.Grade) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Surah ${state.card.ayah.surahNo} · Ayah ${state.card.ayah.ayahNo}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        if (!state.revealed) {
            Text(
                "Recite from memory, then reveal.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onReveal, modifier = Modifier.fillMaxWidth()) {
                Text("Show ayah")
            }
        } else {
            Text(
                text = state.card.ayah.textUthmani,
                style = ArabicAyahTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "How well did you recall it?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GradeButton("Again", Sm2.Grade.AGAIN, onGrade, Modifier.weight(1f))
                GradeButton("Hard", Sm2.Grade.HARD, onGrade, Modifier.weight(1f))
                GradeButton("Good", Sm2.Grade.GOOD, onGrade, Modifier.weight(1f))
                GradeButton("Easy", Sm2.Grade.EASY, onGrade, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GradeButton(
    label: String,
    grade: Sm2.Grade,
    onGrade: (Sm2.Grade) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = { onGrade(grade) },
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Text(label, maxLines = 1, style = MaterialTheme.typography.labelLarge)
    }
}
