package com.the5watermelons.motionpulse.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.the5watermelons.motionpulse.databinding.FragmentHomeBinding
import com.the5watermelons.motionpulse.databinding.ItemHabitRowBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // TODO (Habits step): replace this static list with real data from
    // Room/Firestore once habit CRUD is wired up.
    private val placeholderHabits = listOf("Drink Water", "Read a Book", "Take a walk")

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

        populateHabitList()
    }

    private fun populateHabitList() {
        binding.habitListContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        placeholderHabits.forEach { habitName ->
            val rowBinding = ItemHabitRowBinding.inflate(inflater, binding.habitListContainer, false)
            rowBinding.tvHabitName.text = habitName
            binding.habitListContainer.addView(rowBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}