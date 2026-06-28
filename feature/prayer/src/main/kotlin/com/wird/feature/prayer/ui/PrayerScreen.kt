package com.wird.feature.prayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.wird.feature.prayer.data.CITIES
import com.wird.feature.prayer.data.City

private val METHOD_OPTIONS = listOf(
    CalculationMethod.MUSLIM_WORLD_LEAGUE to "Muslim World League",
    CalculationMethod.KARACHI to "Karachi",
    CalculationMethod.EGYPTIAN to "Egyptian",
    CalculationMethod.UMM_AL_QURA to "Umm al-Qura (Makkah)",
    CalculationMethod.DUBAI to "Dubai",
    CalculationMethod.QATAR to "Qatar",
    CalculationMethod.KUWAIT to "Kuwait",
    CalculationMethod.SINGAPORE to "Singapore",
    CalculationMethod.NORTH_AMERICA to "ISNA (North America)",
)

private fun methodLabel(method: CalculationMethod): String =
    METHOD_OPTIONS.firstOrNull { it.first == method }?.second ?: method.name

@Composable
fun PrayerRoute(viewModel: PrayerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PrayerScreen(
        uiState = uiState,
        onMethodChange = viewModel::setMethod,
        onMadhabChange = viewModel::setMadhab,
        onCityChange = viewModel::setCity,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    uiState: PrayerUiState,
    onMethodChange: (CalculationMethod) -> Unit,
    onMadhabChange: (Madhab) -> Unit,
    onCityChange: (City) -> Unit,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showCityPicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Prayer Times") },
                actions = {
                    IconButton(onClick = { showCityPicker = true }) {
                        Icon(Icons.Default.Place, contentDescription = "Change location")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Calculation settings")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "${uiState.locationName} · ${uiState.dateLabel}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${methodLabel(uiState.method)} · ${asrLabel(uiState.madhab)} asr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            uiState.times.forEach { row ->
                ListItem(
                    headlineContent = { Text(row.name) },
                    trailingContent = {
                        Text(row.time, style = MaterialTheme.typography.titleMedium)
                    },
                )
                HorizontalDivider()
            }
        }

        if (showSettings) {
            SettingsSheet(
                selectedMethod = uiState.method,
                selectedMadhab = uiState.madhab,
                onMethodChange = onMethodChange,
                onMadhabChange = onMadhabChange,
                onDismiss = { showSettings = false },
            )
        }

        if (showCityPicker) {
            CityPickerSheet(
                onCityChange = onCityChange,
                onDismiss = { showCityPicker = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityPickerSheet(
    onCityChange: (City) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) {
            CITIES
        } else {
            CITIES.filter { it.name.contains(q, true) || it.country.contains(q, true) }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxHeight(0.9f)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search city") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            LazyColumn(Modifier.fillMaxWidth()) {
                items(filtered, key = { "${it.name}-${it.country}" }) { city ->
                    ListItem(
                        modifier = Modifier.clickable {
                            onCityChange(city)
                            onDismiss()
                        },
                        headlineContent = { Text(city.name) },
                        supportingContent = { Text(city.country) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun asrLabel(madhab: Madhab): String =
    if (madhab == Madhab.HANAFI) "Hanafi" else "Standard"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    selectedMethod: CalculationMethod,
    selectedMadhab: Madhab,
    onMethodChange: (CalculationMethod) -> Unit,
    onMadhabChange: (Madhab) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text("Calculation method", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            METHOD_OPTIONS.forEach { (method, label) ->
                OptionRow(
                    label = label,
                    selected = method == selectedMethod,
                    onClick = { onMethodChange(method) },
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Asr calculation", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OptionRow(
                label = "Standard (Shafi'i, Maliki, Hanbali)",
                selected = selectedMadhab != Madhab.HANAFI,
                onClick = { onMadhabChange(Madhab.SHAFI) },
            )
            OptionRow(
                label = "Hanafi",
                selected = selectedMadhab == Madhab.HANAFI,
                onClick = { onMadhabChange(Madhab.HANAFI) },
            )
        }
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
