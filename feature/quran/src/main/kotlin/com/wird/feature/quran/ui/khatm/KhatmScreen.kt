package com.wird.feature.quran.ui.khatm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.feature.quran.data.KhatmUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun KhatmRoute(
    onBack: () -> Unit,
    viewModel: KhatmViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    KhatmScreen(
        uiState = uiState,
        onBack = onBack,
        onCreatePlan = viewModel::createPlan,
        onClearPlan = viewModel::clearPlan,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhatmScreen(
    uiState: KhatmUiState,
    onBack: () -> Unit,
    onCreatePlan: (Long) -> Unit,
    onClearPlan: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khatm plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (uiState) {
                KhatmUiState.NoPlan -> NoPlanContent(onCreatePlan)
                is KhatmUiState.Active -> ActivePlanContent(uiState, onCreatePlan, onClearPlan)
            }
        }
    }
}

@Composable
private fun NoPlanContent(onCreatePlan: (Long) -> Unit) {
    Text(
        "Set a finish date and Wird will pace your daily reading to complete the Quran.",
        style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(Modifier.height(8.dp))
    PlanDatePickerButton(label = "Set finish date", onPicked = onCreatePlan)
}

@Composable
private fun ActivePlanContent(
    state: KhatmUiState.Active,
    onCreatePlan: (Long) -> Unit,
    onClearPlan: () -> Unit,
) {
    val target = LocalDate.ofEpochDay(state.targetEpochDay)
        .format(DateTimeFormatter.ofPattern("d MMM yyyy"))

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Finish by $target", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
            Text(
                "${state.ayatRead} / ${state.totalAyat} ayat  ·  ${(state.progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    StatRow("Read today's portion", "${state.todayPace} ayat/day left")
    StatRow("Days remaining", "${state.daysRemaining}")
    StatRow("Planned pace", "${state.dailyTarget} ayat/day")

    val statusText: String
    val statusColor = when {
        state.finished -> {
            statusText = "Khatm complete — masha'Allah!"
            MaterialTheme.colorScheme.primary
        }
        state.aheadBy >= 0 -> {
            statusText = "On track (ahead by ${state.aheadBy} ayat)"
            MaterialTheme.colorScheme.primary
        }
        else -> {
            statusText = "Behind by ${-state.aheadBy} ayat — read ${state.todayPace} today to catch up"
            MaterialTheme.colorScheme.error
        }
    }
    Text(statusText, color = statusColor, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

    Spacer(Modifier.height(8.dp))
    PlanDatePickerButton(label = "Change finish date", onPicked = onCreatePlan)
    OutlinedButton(onClick = onClearPlan, modifier = Modifier.fillMaxWidth()) {
        Text("Clear plan")
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanDatePickerButton(label: String, onPicked: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    val defaultTarget = remember {
        LocalDate.now().plusDays(30).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    Button(onClick = { show = true }, modifier = Modifier.fillMaxWidth()) { Text(label) }

    if (show) {
        val todayUtc = remember {
            LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = defaultTarget,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis > todayUtc
            },
        )
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let(onPicked)
                        show = false
                    },
                ) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }
}
