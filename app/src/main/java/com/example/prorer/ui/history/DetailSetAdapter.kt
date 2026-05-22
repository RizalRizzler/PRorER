package com.example.prorer.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.prorer.R
import com.example.prorer.data.model.SetEntry
import com.example.prorer.ui.shared.UnitFormatter

class DetailSetAdapter : ListAdapter<SetEntry, DetailSetAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //reuse log screen view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_set, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSetNum: TextView = view.findViewById(R.id.tv_set_number)
        private val etWeight: EditText = view.findViewById(R.id.et_weight)
        private val etReps: EditText = view.findViewById(R.id.et_reps)
        private val cbComplete: CheckBox = view.findViewById(R.id.cb_complete)
        private val tvPrBadge: TextView = view.findViewById(R.id.tv_pr_badge)

        fun bind(set: SetEntry) {
            tvSetNum.text = set.setNumber.toString()

            //format
            val isLbs = UnitFormatter.isLbs(itemView.context)
            etWeight.setText(UnitFormatter.displayWeight(set.weightKg, isLbs))
            etReps.setText(set.reps.toString())

            cbComplete.isChecked = set.isCompleted
            tvPrBadge.visibility = View.GONE

            //set to read only
            etWeight.isEnabled = false
            etReps.isEnabled = false
            cbComplete.isEnabled = false
            etWeight.setTextColor(itemView.resources.getColor(android.R.color.black, null))
            etReps.setTextColor(itemView.resources.getColor(android.R.color.black, null))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SetEntry>() {
        override fun areItemsTheSame(oldItem: SetEntry, newItem: SetEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SetEntry, newItem: SetEntry) = oldItem == newItem
    }
}