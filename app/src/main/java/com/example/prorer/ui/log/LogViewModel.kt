package com.example.prorer.ui.log

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prorer.data.model.Exercise
import com.example.prorer.data.model.Session
import com.example.prorer.data.model.SetEntry
import com.example.prorer.data.repository.LiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class LogUiState(
    val activeExercises: List<ActiveExercise> = emptyList(),
    val notes: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null // Replaces isSaved boolean to prevent loops
) : Parcelable

@Parcelize
data class ActiveExercise(
    val internalId: String,
    val name: String,
    val sets: List<ActiveSet>,
    val historicalMaxWeight: Float
) : Parcelable

@Parcelize
data class ActiveSet(
    val internalId: String,
    val setNumber: Int,
    val reps: String,
    val weightInput: String,
    val parsedKg: Float = 0f,
    val isCompleted: Boolean,
    val isPR: Boolean,
    val ghostWeight: String, // Split for cleaner UI
    val ghostReps: String    // Split for cleaner UI
) : Parcelable

class LogViewModel(
    private val repository: LiftRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        savedStateHandle.get<LogUiState>("saved_ui_state") ?: LogUiState()
    )
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.collect { state ->
                savedStateHandle["saved_ui_state"] = state
            }
        }
    }

    fun addExercise(name: String) {
        if (_uiState.value.activeExercises.any { it.name.equals(name, ignoreCase = true) }) {
            _uiState.update { it.copy(errorMessage = "$name is already in this session.") }
            return
        }

        viewModelScope.launch {
            val maxWeight = repository.getMaxWeightForExercise(name) ?: 0f
            val ghostSets = repository.getGhostSetsForExercise(name)

            val initialSet = ActiveSet(
                internalId = UUID.randomUUID().toString(),
                setNumber = 1,
                reps = "",
                weightInput = "",
                parsedKg = 0f,
                isCompleted = false,
                isPR = false,
                ghostWeight = if (ghostSets.isNotEmpty()) ghostSets[0].weightKg.toString() else "",
                ghostReps = if (ghostSets.isNotEmpty()) ghostSets[0].reps.toString() else ""
            )

            _uiState.update { state ->
                val newExercise = ActiveExercise(
                    internalId = UUID.randomUUID().toString(),
                    name = name,
                    sets = listOf(initialSet),
                    historicalMaxWeight = maxWeight
                )
                state.copy(activeExercises = state.activeExercises + newExercise, errorMessage = null)
            }
        }
    }

    fun addSetToExercise(exerciseId: String) {
        _uiState.update { state ->
            val updated = state.activeExercises.map { ex ->
                if (ex.internalId == exerciseId) {
                    val lastWeight = ex.sets.lastOrNull()?.weightInput ?: ""
                    val newSet = ActiveSet(
                        internalId = UUID.randomUUID().toString(),
                        setNumber = ex.sets.size + 1,
                        reps = "",
                        weightInput = lastWeight,
                        parsedKg = 0f,
                        isCompleted = false,
                        isPR = false,
                        ghostWeight = "",
                        ghostReps = ""
                    )
                    ex.copy(sets = ex.sets + newSet)
                } else ex
            }
            state.copy(activeExercises = updated)
        }
    }

    // NEW: Live syncs text to prevent data loss when UI refreshes
    fun updateSetInput(exerciseId: String, setId: String, reps: String, weightInput: String) {
        _uiState.update { state ->
            val updated = state.activeExercises.map { ex ->
                if (ex.internalId == exerciseId) {
                    val updatedSets = ex.sets.map { set ->
                        if (set.internalId == setId) set.copy(reps = reps, weightInput = weightInput) else set
                    }
                    ex.copy(sets = updatedSets)
                } else ex
            }
            state.copy(activeExercises = updated)
        }
    }

    fun toggleSetComplete(exerciseId: String, setId: String, isChecked: Boolean, parsedKg: Float) {
        _uiState.update { state ->
            val updated = state.activeExercises.map { ex ->
                if (ex.internalId == exerciseId) {
                    val updatedSets = ex.sets.map { set ->
                        if (set.internalId == setId) {
                            val isPR = isChecked && parsedKg > 0f && parsedKg > ex.historicalMaxWeight
                            set.copy(
                                parsedKg = parsedKg,
                                isCompleted = isChecked,
                                isPR = isPR
                            )
                        } else set
                    }
                    ex.copy(sets = updatedSets)
                } else ex
            }
            state.copy(activeExercises = updated)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun saveSession(notes: String) {
        val state = _uiState.value
        if (state.activeExercises.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Cannot save an empty workout.") }
            return
        }

        viewModelScope.launch {
            val session = Session(dateMs = System.currentTimeMillis(), notes = notes)
            val sessionId = repository.insertSession(session)
            var totalValidSets = 0

            state.activeExercises.forEachIndexed { index, activeEx ->
                val validSets = activeEx.sets.filter { it.isCompleted && it.parsedKg > 0f && (it.reps.toIntOrNull() ?: 0) > 0 }

                if (validSets.isNotEmpty()) {
                    val exerciseId = repository.insertExercise(Exercise(sessionId = sessionId, name = activeEx.name, orderIndex = index))
                    validSets.forEach { activeSet ->
                        repository.insertSet(SetEntry(
                            exerciseId = exerciseId,
                            setNumber = activeSet.setNumber,
                            reps = activeSet.reps.toInt(),
                            weightKg = activeSet.parsedKg,
                            isCompleted = true
                        ))
                        totalValidSets++
                    }
                }
            }

            if (totalValidSets == 0) {
                repository.deleteSession(session.copy(id = sessionId))
                _uiState.update { it.copy(errorMessage = "No valid completed sets to save. Ensure checkboxes are ticked.") }
            } else {
                // Completely wipe the in-memory state on success to prevent recreate() loops
                _uiState.value = LogUiState(successMessage = "Workout Saved!")
            }
        }
    }
}