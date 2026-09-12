package com.the5watermelons.motionpulse.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.the5watermelons.motionpulse.data.local.HabitEntity
import com.the5watermelons.motionpulse.databinding.FragmentHomeBinding
import com.the5watermelons.motionpulse.databinding.ItemHabitRowBinding
import com.the5watermelons.motionpulse.ui.habits.HabitsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Shared with HabitsFragment via activityViewModels -- same live data,
    // so completing a habit here or on the Habits tab stays in sync instantly.
    private val viewModel: HabitsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayName = FirebaseAuth.getInstance().currentUser?.displayName
        val firstName = displayName?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "there"
        binding.tvGreeting.text = "Welcome back $firstName! \uD83D\uDC4B"

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habits.collectLatest { habits ->
                populateHabitList(habits)
            }
        }
    }

    private fun populateHabitList(habits: List<HabitEntity>) {
        binding.habitListContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        if (habits.isEmpty()) {
            return
        }

        habits.forEach { habit ->
            val rowBinding = ItemHabitRowBinding.inflate(inflater, binding.habitListContainer, false)
            rowBinding.tvHabitName.text = habit.name
            rowBinding.tvBadge.alpha = if (habit.isDoneToday) 1f else 0.5f
            rowBinding.root.setOnClickListener { viewModel.toggleComplete(habit) }
            binding.habitListContainer.addView(rowBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}