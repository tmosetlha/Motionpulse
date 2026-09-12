package com.the5watermelons.motionpulse.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.databinding.FragmentHabitsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HabitsViewModel by activityViewModels()
    private lateinit var adapter: HabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HabitAdapter(
            onToggle = { habit -> viewModel.toggleComplete(habit) },
            onLongPressDelete = { habit -> confirmDelete(habit) }
        )
        binding.rvHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHabits.adapter = adapter

        binding.fabAddHabit.setOnClickListener {
            findNavController().navigate(R.id.action_habitsFragment_to_addHabitFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habits.collectLatest { habits ->
                adapter.submitList(habits)
                binding.tvEmptyState.visibility = if (habits.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun confirmDelete(habit: com.the5watermelons.motionpulse.data.local.HabitEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle(habit.name)
            .setMessage(getString(R.string.confirm_delete_habit))
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ -> viewModel.deleteHabit(habit) }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}