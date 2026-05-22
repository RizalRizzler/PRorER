package com.example.prorer.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.prorer.LiftApp
import com.example.prorer.R
import com.example.prorer.ui.shared.ViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.launch
import java.util.Date

class ProgressFragment : Fragment(R.layout.fragment_progress) {

    private val viewModel: ProgressViewModel by viewModels {
        ViewModelFactory((requireActivity().application as LiftApp).repository)
    }

    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner = view.findViewById<Spinner>(R.id.spinner_exercises)
        val tv1Rm = view.findViewById<TextView>(R.id.tv_1rm)
        val tvDelta = view.findViewById<TextView>(R.id.tv_delta)
        val tvSessionCount = view.findViewById<TextView>(R.id.tv_session_count)
        val tvTopSets = view.findViewById<TextView>(R.id.tv_top_sets_content)
        val barChart = view.findViewById<BarChart>(R.id.bar_chart)

        setupChart(barChart)

        spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val name = spinnerAdapter.getItem(position)
                if (name != null) viewModel.selectExercise(name)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    if (spinnerAdapter.count != state.exerciseNames.size) {
                        spinnerAdapter.clear()
                        spinnerAdapter.addAll(state.exerciseNames)
                    }

                    val selectedIdx = state.exerciseNames.indexOf(state.selectedExercise)
                    if (selectedIdx >= 0 && spinner.selectedItemPosition != selectedIdx) {
                        spinner.setSelection(selectedIdx)
                    }


                    tv1Rm.text = String.format("%.1f kg", state.current1RM)

                    val deltaPrefix = if (state.delta1RM >= 0) "+" else ""
                    tvDelta.text = String.format("$deltaPrefix%.1f kg vs 30 days", state.delta1RM)
                    tvDelta.setTextColor(if (state.delta1RM >= 0) Color.parseColor("#009900") else Color.RED)

                    tvSessionCount.text = state.sessionCount30Days.toString()


                    val sb = java.lang.StringBuilder()
                    state.topSets.forEachIndexed { index, set ->
                        val date = DateFormat.format("MMM dd", Date(set.dateMs))
                        sb.append("${index + 1}. ${set.weightKg} kg × ${set.reps} reps ($date)\n")
                    }
                    tvTopSets.text = if (sb.isEmpty()) "No sets recorded yet." else sb.toString()

                    updateChartData(barChart, state.weeklyVolume)
                }
            }
        }
    }

    private fun setupChart(chart: BarChart) {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.legend.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
            arrayOf("7w", "6w", "5w", "4w", "3w", "2w", "1w", "This Wk")
        )

        chart.axisRight.isEnabled = false
        chart.axisLeft.axisMinimum = 0f
    }

    private fun updateChartData(chart: BarChart, volumeData: List<WeeklyVolume>) {
        if (volumeData.isEmpty()) {
            chart.clear()
            return
        }

        val entries = ArrayList<BarEntry>()
        //volumeData is sorted by weeksAgo descending (7 to 0).
        //map into array index 0 to 7 for the chart x-axis.
        volumeData.forEachIndexed { index, weeklyVolume ->
            entries.add(BarEntry(index.toFloat(), weeklyVolume.volumeKg))
        }

        val dataSet = BarDataSet(entries, "Volume")
        dataSet.color = Color.parseColor("#3F51B5")
        dataSet.valueTextSize = 10f

        chart.data = BarData(dataSet)
        chart.invalidate() //refresh
    }
}