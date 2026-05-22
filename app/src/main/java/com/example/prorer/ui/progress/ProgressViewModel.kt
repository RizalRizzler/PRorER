package com.example.prorer.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prorer.data.model.SetWithDate
import com.example.prorer.data.repository.LiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeeklyVolume(val weeksAgo: Int, val volumeKg: Float)

data class ProgressUiState(
    val exerciseNames: List<String> = emptyList(),
    val selectedExercise: String? = null,
    val current1RM: Float = 0f,
    val delta1RM: Float = 0f,
    val sessionCount30Days: Int = 0,
    val topSets: List<SetWithDate> = emptyList(),
    val weeklyVolume: List<WeeklyVolume> = emptyList()
)

class ProgressViewModel(private val repository: LiftRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.distinctExerciseNames.collectLatest { names ->
                _uiState.update { it.copy(exerciseNames = names) }
                if (names.isNotEmpty() && _uiState.value.selectedExercise == null) {
                    selectExercise(names.first())
                }
            }
        }
    }

    fun selectExercise(name: String) {
        if (name == _uiState.value.selectedExercise) return

        viewModelScope.launch {
            repository.getHistoryForExercise(name).collectLatest { history ->
                val state = calculateStats(name, history)
                _uiState.value = state
            }
        }
    }

    private fun calculateStats(name: String, history: List<SetWithDate>): ProgressUiState {
        if (history.isEmpty()) return ProgressUiState(selectedExercise = name)

        val nowMs = System.currentTimeMillis()
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        val cutoff30DaysMs = nowMs - thirtyDaysMs

        //Epley 1RM calc
        fun calculate1RM(weight: Float, reps: Int) = weight * (1f + reps / 30f)

        //find 1RM OAT, and best prior to 30 days ago to calculate delta
        var best1RmAllTime = 0f
        var best1RmPast = 0f
        var sessionCount30Days = 0

        //a set to count unique sessions based on exact timestamps
        val recentSessions = mutableSetOf<Long>()

        history.forEach { set ->
            val oneRm = calculate1RM(set.weightKg, set.reps)
            if (oneRm > best1RmAllTime) best1RmAllTime = oneRm

            if (set.dateMs < cutoff30DaysMs) {
                if (oneRm > best1RmPast) best1RmPast = oneRm
            } else {
                recentSessions.add(set.dateMs)
            }
        }

        sessionCount30Days = recentSessions.size
        val delta = if (best1RmPast > 0f) best1RmAllTime - best1RmPast else 0f

        //top 3 Sets by heaviest weight, then most reps
        val topSets = history.sortedWith(compareByDescending<SetWithDate> { it.weightKg }
            .thenByDescending { it.reps })
            .take(3)

        //weekly volume by 8 weeks
        val oneWeekMs = 7L * 24 * 60 * 60 * 1000
        val volumeMap = mutableMapOf<Int, Float>()

        //declare 8 weeks with 0 volume
        for (i in 0..7) volumeMap[i] = 0f

        history.forEach { set ->
            val weeksAgo = ((nowMs - set.dateMs) / oneWeekMs).toInt()
            if (weeksAgo in 0..7) {
                val currentVol = volumeMap[weeksAgo] ?: 0f
                volumeMap[weeksAgo] = currentVol + (set.weightKg * set.reps)
            }
        }

        val weeklyVolume = volumeMap.map { WeeklyVolume(it.key, it.value) }.sortedByDescending { it.weeksAgo }

        return _uiState.value.copy(
            selectedExercise = name,
            current1RM = best1RmAllTime,
            delta1RM = delta,
            sessionCount30Days = sessionCount30Days,
            topSets = topSets,
            weeklyVolume = weeklyVolume
        )
    }
}