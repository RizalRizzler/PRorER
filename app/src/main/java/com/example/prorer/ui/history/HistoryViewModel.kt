package com.example.prorer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prorer.data.model.SessionWithDetails
import com.example.prorer.data.repository.LiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(repository: LiftRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val uiState: StateFlow<List<SessionWithDetails>> = combine(
        repository.allSessionsWithDetails,
        _searchQuery
    ) { sessions, query ->
        if (query.isBlank()) {
            sessions
        } else {
            sessions.filter { session ->
                session.exercises.any { it.exercise.name.contains(query, ignoreCase = true) }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}