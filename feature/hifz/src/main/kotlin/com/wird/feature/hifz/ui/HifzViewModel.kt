package com.wird.feature.hifz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.core.database.entity.SurahEntity
import com.wird.feature.hifz.data.HifzRepository
import com.wird.feature.hifz.data.MemorizingSurah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HifzDashboardState(
    val totalCount: Int = 0,
    val dueCount: Int = 0,
    val surahs: List<SurahEntity> = emptyList(),
    val memorizing: List<MemorizingSurah> = emptyList(),
)

@HiltViewModel
class HifzViewModel @Inject constructor(
    private val repository: HifzRepository,
) : ViewModel() {

    val uiState: StateFlow<HifzDashboardState> = combine(
        repository.observeTotalCount(),
        repository.observeDueCount(),
        repository.observeSurahs(),
        repository.observeMemorizingSurahs(),
    ) { total, due, surahs, memorizing ->
        HifzDashboardState(
            totalCount = total,
            dueCount = due,
            surahs = surahs,
            memorizing = memorizing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HifzDashboardState(),
    )

    fun addSurah(surahNo: Int) {
        viewModelScope.launch { repository.addSurah(surahNo) }
    }
}
