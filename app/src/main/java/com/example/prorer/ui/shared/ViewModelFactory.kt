package com.example.prorer.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.prorer.data.repository.LiftRepository
import com.example.prorer.ui.history.HistoryViewModel
import com.example.prorer.ui.history.SessionDetailViewModel
import com.example.prorer.ui.log.LogViewModel
import com.example.prorer.ui.progress.ProgressViewModel

class ViewModelFactory(private val repository: LiftRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val handle = extras.createSavedStateHandle()

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(LogViewModel::class.java) -> LogViewModel(repository, handle) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(repository) as T
            modelClass.isAssignableFrom(SessionDetailViewModel::class.java) -> SessionDetailViewModel(repository) as T
            modelClass.isAssignableFrom(ProgressViewModel::class.java) -> ProgressViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}