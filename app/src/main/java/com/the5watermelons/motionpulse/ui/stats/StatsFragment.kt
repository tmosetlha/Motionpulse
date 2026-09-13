package com.the5watermelons.motionpulse.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.the5watermelons.motionpulse.databinding.FragmentStatsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()
    private lateinit var adapter: StatsHabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StatsHabitAdapter()
        binding.rvWeeklyStats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWeeklyStats.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.summary.collectLatest { summary ->
                binding.tvDoneToday.text = "${summary.doneToday}/${summary.totalHabits}"
                binding.tvCurrentStreak.text = "${summary.currentStreak}d"
                binding.tvTotalCompleted.text = summary.totalCompleted.toString()
                binding.tvWeekPercent.text = "${summary.weekPercent}%"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.weeklyData.collectLatest { weekly ->
                adapter.submitList(weekly)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}