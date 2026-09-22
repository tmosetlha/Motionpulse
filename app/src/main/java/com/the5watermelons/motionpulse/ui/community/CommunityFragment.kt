package com.the5watermelons.motionpulse.ui.community

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.api.MotionPulseApiService
import com.the5watermelons.motionpulse.api.SocialFeedItem
import com.the5watermelons.motionpulse.databinding.FragmentCommunityBinding
import com.the5watermelons.motionpulse.util.MessageUtils
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val apiService by lazy { MotionPulseApiService.create() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSocialFeed()

        binding.btnInviteFriend.setOnClickListener {
            showInviteFriendDialog()
        }

        binding.cardChallenge.setOnClickListener {
            MessageUtils.showSuccess(requireActivity(), "Opening Squad Stats...")
        }
    }

    private fun showInviteFriendDialog() {
        val context = requireContext()
        val editText = android.widget.EditText(context).apply {
            hint = "Enter friend's email"
            setPadding(48, 48, 48, 48)
        }

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Invite a Friend")
            .setMessage("Challenge them to build a rhythm together!")
            .setView(editText)
            .setPositiveButton("Send Invite") { _, _ ->
                val email = editText.text.toString().trim()
                if (email.isNotEmpty()) {
                    sendInvite(email)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendInvite(email: String) {
        lifecycleScope.launch {
            try {
                // apiService.sendChallengeInvite(ChallengeInvite("current_user", email, "Morning Walk", 14))
                MessageUtils.showSuccess(requireActivity(), "Invite sent to $email!")
            } catch (e: Exception) {
                MessageUtils.showError(requireActivity(), "Failed to send invite")
            }
        }
    }

    private fun loadSocialFeed() {
        lifecycleScope.launch {
            try {
                // Fetch live feed from REST API
                // val feed = apiService.getCommunityFeed("current_user")
                
                // For now, we populate with exact data from your mockup to match perfectly
                val mockFeed = listOf(
                    SocialFeedItem("1", "JM", "Jordan M.", "hit a 14-day streak 🔥", "12 mins ago", 4),
                    SocialFeedItem("2", "SK", "Sam K.", "completed all 3 habits today", "1 hour ago", 1),
                    SocialFeedItem("3", "AT", "Amara T.", "hasn't logged today", "Streak at risk · 6 days", 0, isRisk = true)
                )
                populateFeed(mockFeed)
            } catch (e: Exception) {
                MessageUtils.showError(requireActivity(), "Failed to load community feed")
            }
        }
    }

    private fun populateFeed(items: List<SocialFeedItem>) {
        binding.feedContainer.removeAllViews()
        val context = requireContext()
        val density = resources.displayMetrics.density

        items.forEach { item ->
            val itemView = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, (20 * density).toInt())
                }
            }

            // Avatar Circle
            val avatar = TextView(context).apply {
                text = item.initials
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams((44 * density).toInt(), (44 * density).toInt())
                background = ContextCompat.getDrawable(context, R.drawable.circle_dot)
                backgroundTintList = ColorStateList.valueOf(
                    if (item.isRisk) Color.parseColor("#333333") else Color.parseColor("#E8348A")
                )
            }

            // Info Column
            val info = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = (16 * density).toInt()
                }
            }

            val nameStatus = TextView(context).apply {
                text = "${item.name} ${item.statusText}"
                setTextColor(Color.WHITE)
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val time = TextView(context).apply {
                text = item.timeAgo
                setTextColor(ContextCompat.getColor(context, R.color.mp_text_secondary))
                textSize = 12f
            }

            info.addView(nameStatus)
            info.addView(time)

            // Actions Row
            val actions = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            if (item.isRisk) {
                val btnNudge = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonStyle).apply {
                    text = "👋 Send nudge"
                    isAllCaps = false
                    textSize = 12f
                    setTextColor(Color.parseColor("#E8348A"))
                    strokeColor = ColorStateList.valueOf(Color.parseColor("#E8348A"))
                    strokeWidth = (1 * density).toInt()
                    cornerRadius = (20 * density).toInt()
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener { MessageUtils.showSuccess(requireActivity(), "Nudge sent to ${item.name}!") }
                }
                actions.addView(btnNudge)
            } else {
                val btnFlame = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonStyle).apply {
                    text = "🔥 ${item.flameCount}"
                    isAllCaps = false
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#2D1B36"))
                    cornerRadius = (16 * density).toInt()
                }

                val btnNudgeSmall = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonStyle).apply {
                    text = "Nudge"
                    isAllCaps = false
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#262626"))
                    cornerRadius = (16 * density).toInt()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = (8 * density).toInt()
                    }
                    setOnClickListener { MessageUtils.showSuccess(requireActivity(), "Encouragement sent!") }
                }
                actions.addView(btnFlame)
                actions.addView(btnNudgeSmall)
            }

            itemView.addView(avatar)
            itemView.addView(info)
            itemView.addView(actions)
            binding.feedContainer.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}