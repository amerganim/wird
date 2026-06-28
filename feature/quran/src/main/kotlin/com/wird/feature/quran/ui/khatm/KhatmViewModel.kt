package com.wird.feature.quran.ui.khatm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.feature.quran.data.KhatmRepository
import com.wird.feature.quran.data.KhatmUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class KhatmViewModel @Inject constructor(
    private val repository: KhatmRepository,
) : ViewModel() {

    val uiState: StateFlow<KhatmUiState> = repository.observeState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KhatmUiState.NoPlan,
    )

    /** [targetMillis] is the UTC midnight epoch-millis returned by the date picker. */
    fun createPlan(targetMillis: Long) {
        val epochDay = Instant.ofEpochMilli(targetMillis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
        viewModelScope.launch { repository.createPlan(epochDay) }
    }

    fun clearPlan() {
        viewModelScope.launch { repository.clearPlan() }
    }
}
