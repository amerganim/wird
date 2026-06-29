package com.wird.feature.quran.ui.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.feature.quran.data.DailyAyahItem
import com.wird.feature.quran.data.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyUiState(
    val ayat: List<DailyAyahItem> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val readToday: Boolean = false,
)

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    val uiState: StateFlow<DailyUiState> = combine(
        flow { emit(repository.getDailyAyat()) },
        repository.observeState(),
    ) { ayat, state ->
        DailyUiState(
            ayat = ayat,
            currentStreak = state.currentStreak,
            bestStreak = state.bestStreak,
            readToday = state.readToday,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyUiState(),
    )

    fun markRead() {
        viewModelScope.launch { repository.markReadToday() }
    }
}
