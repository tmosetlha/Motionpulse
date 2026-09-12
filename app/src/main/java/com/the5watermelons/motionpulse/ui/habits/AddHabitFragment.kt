package com.the5watermelons.motionpulse.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.the5watermelons.motionpulse.databinding.FragmentAddHabitBinding

class AddHabitFragment : Fragment() {

    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HabitsViewModel by activityViewModels()

    private var selectedCategory: String = "Mind & Focus"
    private lateinit var chips: List<TextView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chips = listOf(binding.chipMindFocus, binding.chipFitnessHealth, binding.chipDailyRoutine)
        selectChip(binding.chipMindFocus, "Mind & Focus")

        binding.chipMindFocus.setOnClickListener { selectChip(binding.chipMindFocus, "Mind & Focus") }
        binding.chipFitnessHealth.setOnClickListener { selectChip(binding.chipFitnessHealth, "Fitness & Health") }
        binding.chipDailyRoutine.setOnClickListener { selectChip(binding.chipDailyRoutine, "Daily Routine") }

        binding.btnAddHabit.setOnClickListener { submitHabit() }
    }

    private fun selectChip(selected: TextView, category: String) {
        selectedCategory = category
        chips.forEach { it.isSelected = (it == selected) }
    }

    private fun submitHabit() {
        val name = binding.etHabitName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etHabitName.error = "Enter a habit name"
            return
        }
        viewModel.addHabit(name, selectedCategory)
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}