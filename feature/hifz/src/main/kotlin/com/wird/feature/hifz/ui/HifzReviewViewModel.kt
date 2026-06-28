package com.wird.feature.hifz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.feature.hifz.data.HifzRepository
import com.wird.feature.hifz.data.ReviewCard
import com.wird.feature.hifz.data.Sm2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReviewUiState {
    data object Loading : ReviewUiState
    data object Done : ReviewUiState
    data class Reviewing(
        val card: ReviewCard,
        val revealed: Boolean,
        val position: Int,
        val total: Int,
    ) : ReviewUiState
}

@HiltViewModel
class HifzReviewViewModel @Inject constructor(
    private val repository: HifzRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    private var cards: List<ReviewCard> = emptyList()
    private var index = 0

    init {
        viewModelScope.launch {
            cards = repository.getDueCards()
            showCurrent()
        }
    }

    private fun showCurrent() {
        _state.value = if (index >= cards.size) {
            ReviewUiState.Done
        } else {
            ReviewUiState.Reviewing(
                card = cards[index],
                revealed = false,
                position = index + 1,
                total = cards.size,
            )
        }
    }

    fun reveal() {
        val current = _state.value
        if (current is ReviewUiState.Reviewing) {
            _state.value = current.copy(revealed = true)
        }
    }

    fun grade(grade: Sm2.Grade) {
        val current = _state.value
        if (current is ReviewUiState.Reviewing) {
            viewModelScope.launch {
                repository.grade(current.card.ayah.id, grade)
                index++
                showCurrent()
            }
        }
    }
}
