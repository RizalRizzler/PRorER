package com.example.prorer.ui.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.prorer.R

class ExerciseAdapter(
    private val onAddSet: (exerciseId: String) -> Unit,
    private val onInputChanged: (exerciseId: String, setId: String, reps: String, weightInput: String) -> Unit,
    private val onSetToggled: (exerciseId: String, setId: String, reps: String, weightInput: String, isChecked: Boolean) -> Unit
) : ListAdapter<ActiveExercise, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        private val rvSets: RecyclerView = itemView.findViewById(R.id.rv_sets)
        private val btnAddSet: Button = itemView.findViewById(R.id.btn_add_set)

        private val setAdapter = SetAdapter(
            onInputChanged = { setId, reps, weightInput ->
                val exerciseId = getItem(bindingAdapterPosition).internalId
                onInputChanged(exerciseId, setId, reps, weightInput)
            },
            onSetToggled = { setId, isChecked ->
                // Look up current text at the moment of checking the box
                val exercise = getItem(bindingAdapterPosition)
                val set = exercise.sets.find { it.internalId == setId }
                if (set != null) {
                    onSetToggled(exercise.internalId, setId, set.reps, set.weightInput, isChecked)
                }
            }
        )

        init {
            rvSets.layoutManager = LinearLayoutManager(itemView.context)
            rvSets.adapter = setAdapter
            rvSets.isNestedScrollingEnabled = false
        }

        fun bind(exercise: ActiveExercise) {
            tvName.text = exercise.name
            setAdapter.submitList(exercise.sets)
            btnAddSet.setOnClickListener { onAddSet(exercise.internalId) }
        }
    }
}

class ExerciseDiffCallback : DiffUtil.ItemCallback<ActiveExercise>() {
    override fun areItemsTheSame(oldItem: ActiveExercise, newItem: ActiveExercise) = oldItem.internalId == newItem.internalId

    override fun areContentsTheSame(oldItem: ActiveExercise, newItem: ActiveExercise): Boolean {
        if (oldItem.name != newItem.name) return false
        if (oldItem.sets.size != newItem.sets.size) return false

        // check status
        for (i in oldItem.sets.indices) {
            if (oldItem.sets[i].isCompleted != newItem.sets[i].isCompleted) return false
            if (oldItem.sets[i].isPR != newItem.sets[i].isPR) return false
        }

        // (only input text changed)return true to avoid redraw
        return true
    }
}