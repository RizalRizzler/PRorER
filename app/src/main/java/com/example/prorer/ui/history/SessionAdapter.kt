package com.example.prorer.ui.history

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.prorer.R
import com.example.prorer.data.model.SessionWithDetails
import java.util.Date

class SessionAdapter(private val onSessionClick: (Long) -> Unit) :
    ListAdapter<SessionWithDetails, SessionAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvVolume: TextView = itemView.findViewById(R.id.tv_volume)
        private val tvExercises: TextView = itemView.findViewById(R.id.tv_exercises_preview)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    onSessionClick(getItem(position).session.id)
                }
            }
        }

        fun bind(data: SessionWithDetails) {
            val date = DateFormat.format("MMM dd, yyyy", Date(data.session.dateMs)).toString()
            tvDate.text = date

            //calculate total volume for completed sets
            var totalVolume = 0f
            val exerciseNames = mutableListOf<String>()

            data.exercises.forEach { exWithSets ->
                exerciseNames.add(exWithSets.exercise.name)
                exWithSets.sets.filter { it.isCompleted }.forEach { set ->
                    totalVolume += (set.reps * set.weightKg)
                }
            }

            tvVolume.text = String.format("Vol: %.1f kg", totalVolume)
            tvExercises.text = exerciseNames.joinToString(", ")
        }
    }
}

class SessionDiffCallback : DiffUtil.ItemCallback<SessionWithDetails>() {
    override fun areItemsTheSame(oldItem: SessionWithDetails, newItem: SessionWithDetails) = oldItem.session.id == newItem.session.id
    override fun areContentsTheSame(oldItem: SessionWithDetails, newItem: SessionWithDetails) = oldItem == newItem
}