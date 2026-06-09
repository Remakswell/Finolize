package com.finolize.app.presentation.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finolize.app.data.local.prefs.PreferenceManager
import com.finolize.app.domain.usecase.CategoryStat
import com.finolize.app.domain.usecase.GetStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class StatsPeriod { TODAY, WEEK, MONTH, YEAR, ALL }

data class StatsUiState(
    val stats: List<CategoryStat> = emptyList(),
    val totalAmount: Double = 0.0,
    val isEmpty: Boolean = true,
    val selectedPeriod: StatsPeriod = StatsPeriod.TODAY,
    val currency: String = "$"
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    getStatsUseCase: GetStatsUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.TODAY)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<StatsUiState> = combine(
        _selectedPeriod
            .flatMapLatest { getStatsUseCase(it) },
        preferenceManager.currencyFlow
    ) { stats, currency ->
        StatsUiState(
            stats = stats,
            totalAmount = stats.sumOf { it.amount },
            isEmpty = stats.isEmpty(),
            selectedPeriod = _selectedPeriod.value,
            currency = currency
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsUiState()
        )

    fun selectPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
    }
}