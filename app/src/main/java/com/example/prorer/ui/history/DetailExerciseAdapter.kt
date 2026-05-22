package com.example.prorer.ui.history

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
import com.example.prorer.data.model.ExerciseWithSets

class DetailExerciseAdapter : ListAdapter<ExerciseWithSets, DetailExerciseAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tv_exercise_name)
        private val rvSets: RecyclerView = view.findViewById(R.id.rv_sets)
        private val btnAddSet: Button = view.findViewById(R.id.btn_add_set)

        private val setAdapter = DetailSetAdapter()

        init {
            rvSets.layoutManager = LinearLayoutManager(view.context)
            rvSets.adapter = setAdapter
            rvSets.isNestedScrollingEnabled = false

            btnAddSet.visibility = View.GONE
        }

        fun bind(data: ExerciseWithSets) {
            tvName.text = data.exercise.name

            val completedSets = data.sets.filter { it.isCompleted }
            setAdapter.submitList(completedSets)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExerciseWithSets>() {
        override fun areItemsTheSame(oldItem: ExerciseWithSets, newItem: ExerciseWithSets) = oldItem.exercise.id == newItem.exercise.id
        override fun areContentsTheSame(oldItem: ExerciseWithSets, newItem: ExerciseWithSets) = oldItem == newItem
    }
}