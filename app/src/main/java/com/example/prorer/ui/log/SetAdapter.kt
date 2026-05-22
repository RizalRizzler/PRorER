package com.example.prorer.ui.log

import android.text.Editable
import android.text.TextWatcher
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
import com.example.prorer.ui.shared.UnitFormatter

class SetAdapter(
    private val onInputChanged: (setId: String, reps: String, weightInput: String) -> Unit,
    private val onSetToggled: (setId: String, isCompleted: Boolean) -> Unit
) : ListAdapter<ActiveSet, SetAdapter.SetViewHolder>(SetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_set, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSetNumber: TextView = itemView.findViewById(R.id.tv_set_number)
        private val etWeight: EditText = itemView.findViewById(R.id.et_weight)
        private val etReps: EditText = itemView.findViewById(R.id.et_reps)
        private val cbComplete: CheckBox = itemView.findViewById(R.id.cb_complete)
        private val tvPrBadge: TextView = itemView.findViewById(R.id.tv_pr_badge)

        private var currentSetId: String? = null

        init {
            val textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (etWeight.hasFocus() || etReps.hasFocus()) {
                        currentSetId?.let { id ->
                            onInputChanged(id, etReps.text.toString(), etWeight.text.toString())
                        }
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            etWeight.addTextChangedListener(textWatcher)
            etReps.addTextChangedListener(textWatcher)
        }

        fun bind(set: ActiveSet) {
            currentSetId = set.internalId
            tvSetNumber.text = set.setNumber.toString()

            // THE FIX: Block setText() if the user is actively typing in this box!
            // This prevents the cursor from jumping and stops the jitter.
            if (!etWeight.hasFocus() && etWeight.text.toString() != set.weightInput) {
                etWeight.setText(set.weightInput)
            }
            if (!etReps.hasFocus() && etReps.text.toString() != set.reps) {
                etReps.setText(set.reps)
            }

            val isLbs = UnitFormatter.isLbs(itemView.context)
            val unitString = if (isLbs) "lbs" else "kg"

            etWeight.hint = if (set.ghostWeight.isNotEmpty()) set.ghostWeight else unitString
            etReps.hint = if (set.ghostReps.isNotEmpty()) set.ghostReps else "reps"

            cbComplete.setOnCheckedChangeListener(null)
            cbComplete.isChecked = set.isCompleted

            etWeight.isEnabled = !set.isCompleted
            etReps.isEnabled = !set.isCompleted
            tvPrBadge.visibility = if (set.isPR) View.VISIBLE else View.GONE

            cbComplete.setOnCheckedChangeListener { _, isChecked ->
                onSetToggled(set.internalId, isChecked)
            }
        }
    }
}

class SetDiffCallback : DiffUtil.ItemCallback<ActiveSet>() {
    override fun areItemsTheSame(oldItem: ActiveSet, newItem: ActiveSet) = oldItem.internalId == newItem.internalId

    override fun areContentsTheSame(oldItem: ActiveSet, newItem: ActiveSet): Boolean {
        // THE FIX: Ignore text keystrokes so the row doesn't redraw while typing.
        // It will only redraw if they tap the checkbox.
        return oldItem.isCompleted == newItem.isCompleted &&
                oldItem.isPR == newItem.isPR &&
                oldItem.setNumber == newItem.setNumber &&
                oldItem.ghostWeight == newItem.ghostWeight
    }
}