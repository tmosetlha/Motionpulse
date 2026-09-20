package com.the5watermelons.motionpulse.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.databinding.FragmentStatsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val displayName = currentUser?.displayName
        val firstName = displayName?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "User"
        val initials = if (firstName.isNotEmpty()) firstName.take(2).uppercase() else "MP"
        binding.tvInitialsStats.text = initials

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.weeklyData.collectLatest { weekly ->
                populateDotsMatrix(weekly)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moodScores.collectLatest { scores ->
                binding.weeklyLineChart.setDataPoints(scores)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.summary.collectLatest { summary ->
                if (summary.totalHabits > 0) {
                    binding.tvBestDayContent.text = "Today · Energized · ${summary.doneToday}/${summary.totalHabits} habits done"
                    
                    // Dynamic insights update if they completed habits
                    val completedNames = weeklyCompletedHabitNames()
                    if (completedNames.isNotEmpty()) {
                        binding.tvInsightsText.text = "Your mood peaks on days you completed ${completedNames.first()}. Try to keep up this amazing habit momentum!"
                    }
                }
            }
        }
    }

    private fun weeklyCompletedHabitNames(): List<String> {
        val weeklyList = viewModel.weeklyData.value
        return weeklyList.filter { it.weekDots.any { dot -> dot } }.map { it.habit.name }
    }

    private fun populateDotsMatrix(weekly: List<StatsViewModel.HabitWeekly>) {
        binding.weeklyDotsMatrixContainer.removeAllViews()
        val context = requireContext()
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

        val density = context.resources.displayMetrics.density
        val dotSize = (8 * density).toInt()
        val dotMargin = (4 * density).toInt()

        for (dayIndex in 0 until 7) {
            val columnLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            // Add Day Label
            val tvLabel = TextView(context).apply {
                text = dayLabels[dayIndex]
                setTextColor(ContextCompat.getColor(context, R.color.mp_text_secondary))
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, (8 * density).toInt())
            }
            columnLayout.addView(tvLabel)

            // Add dots for each habit on this day
            if (weekly.isEmpty()) {
                // Mock 3 default dots for perfect alignment with design spec if no habits exist
                for (i in 0 until 3) {
                    val dotView = View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                            setMargins(0, dotMargin, 0, dotMargin)
                        }
                        setBackgroundResource(R.drawable.circle_dot)
                        backgroundTintList = android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.mp_text_secondary)
                        )
                        alpha = 0.2f
                    }
                    columnLayout.addView(dotView)
                }
            } else {
                weekly.forEach { habitWeekly ->
                    val done = habitWeekly.weekDots.getOrElse(dayIndex) { false }
                    val dotView = View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                            setMargins(0, dotMargin, 0, dotMargin)
                        }
                        setBackgroundResource(R.drawable.circle_dot)
                        if (done) {
                            backgroundTintList = android.content.res.ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.mp_pink)
                            )
                        } else {
                            backgroundTintList = android.content.res.ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.mp_text_secondary)
                            )
                            alpha = 0.2f
                        }
                    }
                    columnLayout.addView(dotView)
                }
            }

            binding.weeklyDotsMatrixContainer.addView(columnLayout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}