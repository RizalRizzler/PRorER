package com.example.prorer.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prorer.LiftApp
import com.example.prorer.R
import com.example.prorer.ui.shared.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import java.util.Date

class SessionDetailFragment : Fragment(R.layout.fragment_session_detail) {

    private val viewModel: SessionDetailViewModel by viewModels {
        ViewModelFactory((requireActivity().application as LiftApp).repository)
    }

    private lateinit var detailAdapter: DetailExerciseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionId = arguments?.getLong("sessionId") ?: return
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val rvExercises = view.findViewById<RecyclerView>(R.id.rv_detail_exercises)

        detailAdapter = DetailExerciseAdapter()
        rvExercises.layoutManager = LinearLayoutManager(requireContext())
        rvExercises.adapter = detailAdapter

        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        toolbar.menu.clear()
        toolbar.menu.add(0, 1, 0, "Delete").apply {
            setIcon(R.drawable.delete_24px)
            setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        toolbar.setOnMenuItemClickListener {
            showDeleteConfirmation(sessionId)
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getSessionDetails(sessionId).collect { sessionDetails ->
                    if (sessionDetails != null) {
                        val date = DateFormat.format("MMM dd, yyyy", Date(sessionDetails.session.dateMs))
                        toolbar.title = "Workout: $date"

                        detailAdapter.submitList(sessionDetails.exercises)
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation(sessionId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Workout?")
            .setMessage("This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSession(sessionId) {
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}