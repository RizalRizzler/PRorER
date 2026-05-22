package com.example.prorer.ui.history

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
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
import kotlinx.coroutines.launch

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory((requireActivity().application as LiftApp).repository)
    }
    private lateinit var adapter: SessionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHistory = view.findViewById<RecyclerView>(R.id.rv_history)
        val etSearch = view.findViewById<EditText>(R.id.et_search)
        val tvEmptyState = view.findViewById<TextView>(R.id.tv_empty_state)

        adapter = SessionAdapter { sessionId ->
            // Phase 3 Navigation: Pass the ID to Detail Fragment
            val bundle = Bundle().apply { putLong("sessionId", sessionId) }
            findNavController().navigate(R.id.action_historyFragment_to_sessionDetailFragment, bundle)
        }

        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updateSearchQuery(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { sessions ->
                    adapter.submitList(sessions)
                    tvEmptyState.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}