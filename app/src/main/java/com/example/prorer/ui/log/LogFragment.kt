package com.example.prorer.ui.log

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prorer.LiftApp
import com.example.prorer.R
import com.example.prorer.ui.shared.UnitFormatter
import com.example.prorer.ui.shared.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

class LogFragment : Fragment(R.layout.fragment_log) {

    private val viewModel: LogViewModel by viewModels {
        ViewModelFactory((requireActivity().application as LiftApp).repository)
    }

    private lateinit var exerciseAdapter: ExerciseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupToolbar(view)
        setupFab(view)
        observeState()
    }

    private fun setupRecyclerView(view: View) {
        val rvExercises = view.findViewById<RecyclerView>(R.id.rv_exercises)
        exerciseAdapter = ExerciseAdapter(
            onAddSet = { exerciseId -> viewModel.addSetToExercise(exerciseId) },
            onInputChanged = { exId, setId, reps, weightInput ->
                viewModel.updateSetInput(exId, setId, reps, weightInput)
            },
            onSetToggled = { exId, setId, reps, weightInput, isChecked ->
                val isLbs = UnitFormatter.isLbs(requireContext())
                val parsedKg = UnitFormatter.parseInputToKg(weightInput, isLbs)
                viewModel.toggleSetComplete(exId, setId, isChecked, parsedKg)
            }
        )
        rvExercises.layoutManager = LinearLayoutManager(requireContext())
        rvExercises.adapter = exerciseAdapter
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.menu.clear()
        toolbar.menu.add(0, 1, 0, "Save").apply {
            setIcon(R.drawable.save_24px)
            setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        toolbar.menu.add(0, 2, 1, "Settings").apply {
            setIcon(R.drawable.settings_24px)
            setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> { viewModel.saveSession("Workout Notes"); true }
                2 -> { findNavController().navigate(R.id.settingsFragment); true }
                else -> false
            }
        }
    }

    private fun setupFab(view: View) {
        view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_exercise).setOnClickListener {
            showAddExerciseDialog()
        }
    }

    private fun showAddExerciseDialog() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val presetsString = prefs.getString(
            "custom_presets",
            "Bench Press, Squat, Deadlift, Overhead Press, Barbell Row, Pull-Up"
        ) ?: ""

        val presetsList = presetsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val presetsArray = (presetsList + "Custom...").toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Add Exercise")
            .setItems(presetsArray) { _, which ->
                if (which == presetsArray.lastIndex) {
                    showCustomExerciseDialog()
                } else {
                    viewModel.addExercise(presetsArray[which])
                }
            }
            .show()
    }

    private fun showCustomExerciseDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Custom Exercise")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) viewModel.addExercise(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    exerciseAdapter.submitList(state.activeExercises)

                    if (state.successMessage != null) {
                        Toast.makeText(requireContext(), state.successMessage, Toast.LENGTH_SHORT).show()
                        viewModel.clearMessages()
                    }

                    if (state.errorMessage != null) {
                        Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show()
                        viewModel.clearMessages()
                    }
                }
            }
        }
    }
}