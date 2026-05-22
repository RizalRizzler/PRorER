package com.example.prorer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prorer.data.model.SessionWithDetails
import com.example.prorer.data.repository.LiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SessionDetailViewModel(private val repository: LiftRepository) : ViewModel() {

    fun getSessionDetails(sessionId: Long): Flow<SessionWithDetails?> {
        return repository.getSessionWithDetailsById(sessionId)
    }

    fun deleteSession(sessionId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteFullSession(sessionId)
            onDeleted()
        }
    }
}