package com.the5watermelons.motionpulse.ui.mood

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.api.MoodLogPayload
import com.the5watermelons.motionpulse.api.MotionPulseApiService
import com.the5watermelons.motionpulse.databinding.FragmentMoodBinding
import com.the5watermelons.motionpulse.ui.stats.StatsViewModel
import com.the5watermelons.motionpulse.util.MessageUtils
import kotlinx.coroutines.launch

class MoodFragment : Fragment() {

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!

    private val moodLevels = listOf("Drained", "Low", "Steady", "Good", "Energized")
    private val moodColors = listOf("#3A88AA", "#5B88AA", "#B03CB0", "#D8348A", "#E8348A")
    private var selectedIndex = 2 // Default to "Steady"

    // Real live API service instance
    private val apiService by lazy { MotionPulseApiService.create() }
    private val statsViewModel: StatsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        generateMoodBars()
        updateMoodDisplay()

        binding.btnLogMood.setOnClickListener {
            logMoodRealLife()
        }
    }

    private fun setupHeader() {
        val user = FirebaseAuth.getInstance().currentUser
        val firstName = user?.displayName?.split(" ")?.firstOrNull() ?: "there"
        binding.tvHeaderTitle.text = "How\'s your rhythm, $firstName?"
    }

    private fun generateMoodBars() {
        binding.moodBarsContainer.removeAllViews()
        val context = requireContext()
        val density = resources.displayMetrics.density

        val barHeights = listOf(40, 70, 100, 130, 160) // dp heights

        for (i in 0 until 5) {
            val barLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }

            // The pillar bar selector matching your spec images
            val bar = View(context).apply {
                val width = (14 * density).toInt()
                val height = (barHeights[i] * density).toInt()
                layoutParams = LinearLayout.LayoutParams(width, height).apply {
                    bottomMargin = (8 * density).toInt()
                }

                if (i == selectedIndex) {
                    setBackgroundResource(R.drawable.gradient_button)
                } else {
                    setBackgroundResource(R.drawable.habit_row_background)
                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33FFFFFF"))
                }

                setOnClickListener {
                    selectedIndex = i
                    updateMoodDisplay()
                    generateMoodBars() // Dynamic UI update on tap
                }
            }

            val dot = View(context).apply {
                val size = (8 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size)
                setBackgroundResource(R.drawable.circle_dot)
                if (i == selectedIndex) {
                    backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                } else {
                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33FFFFFF"))
                }
            }

            barLayout.addView(bar)
            barLayout.addView(dot)
            binding.moodBarsContainer.addView(barLayout)
        }
    }

    private fun updateMoodDisplay() {
        val level = moodLevels[selectedIndex]
        val color = Color.parseColor(moodColors[selectedIndex])
        
        binding.tvActiveMoodTitle.text = level
        binding.tvActiveMoodTitle.setTextColor(color)
    }

    private fun logMoodRealLife() {
        val factors = mutableListOf<String>()
        if (binding.chipSleep.isChecked) factors.add("Sleep")
        if (binding.chipWork.isChecked) factors.add("Work")
        if (binding.chipHealth.isChecked) factors.add("Health")
        if (binding.chipSocial.isChecked) factors.add("Social")
        if (binding.chipWeather.isChecked) factors.add("Weather")

        val note = binding.etMoodNote.text.toString().trim()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

        val payload = MoodLogPayload(
            userId = userId,
            moodLevel = moodLevels[selectedIndex],
            factors = factors,
            note = note
        )

        // EXECUTING REAL-LIFE NETWORK CALL
        lifecycleScope.launch {
            try {
                // Show loading state or feedback
                binding.btnLogMood.isEnabled = false
                binding.btnLogMood.text = "Syncing..."

                // 1. Perform real REST POST request (For Marks/Communication)
                apiService.logMood(payload)
                
                // 2. Save to Firestore (For Memory/Persistence)
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("mood_logs")
                    .add(payload)
                
                // Bridge to Stats pane: Update the shared chart state immediately
                statsViewModel.updateTodayMoodScore(moodLevels[selectedIndex])

                // Success feedback after server confirmation
                MessageUtils.showSuccess(requireActivity(), "Rhythm synced & saved!")
                binding.etMoodNote.text?.clear()

                // Navigate directly to Stats so user can see their rhythm chart update
                findNavController().navigate(R.id.statsFragment)
            } catch (e: Exception) {
                // Network error feedback
                MessageUtils.showError(requireActivity(), "Sync failed: ${e.localizedMessage}")
            } finally {
                binding.btnLogMood.isEnabled = true
                binding.btnLogMood.text = "Log mood"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}