package com.the5watermelons.motionpulse.ui.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.UserProfileChangeRequest
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.data.local.HabitEntity
import com.the5watermelons.motionpulse.data.notifications.ReminderScheduler
import com.the5watermelons.motionpulse.databinding.FragmentProfileBinding
import com.the5watermelons.motionpulse.databinding.ItemAchievementRowBinding
import com.the5watermelons.motionpulse.util.MessageUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()

    private val prefs by lazy {
        requireContext().getSharedPreferences("motionpulse_prefs", Context.MODE_PRIVATE)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ReminderScheduler.enable(requireContext())
            prefs.edit().putBoolean("reminders_enabled", true).apply()
        } else {
            binding.switchReminder.isChecked = false
            MessageUtils.showError(requireActivity(), "Notifications permission is needed for reminders")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvProfileName.text = auth.currentUser?.displayName ?: "Your Profile"

        binding.switchReminder.isChecked = prefs.getBoolean("reminders_enabled", true)
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableReminders()
            } else {
                ReminderScheduler.disable(requireContext())
                prefs.edit().putBoolean("reminders_enabled", false).apply()
            }
        }

        binding.rowManageProfile.setOnClickListener { showManageProfileDialog() }
        binding.rowChangePassword.setOnClickListener { showChangePasswordDialog() }
        binding.rowLinkedAccounts.setOnClickListener { showLinkedAccountsDialog() }
        binding.rowCopyToken.setOnClickListener { showDeviceTokenDialog() }

        binding.btnLogOut.setOnClickListener {
            auth.signOut()
            val outerNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
            outerNavController.navigate(R.id.action_mainFragment_to_loginFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.summary.collectLatest { summary ->
                binding.tvDayStreak.text = summary.dayStreak.toString()
                binding.tvBadgeCount.text = summary.badgeCount.toString()
                populateAchievements(summary.recentAchievements)
            }
        }
    }

    private fun populateAchievements(achievements: List<HabitEntity>) {
        binding.achievementsContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        if (achievements.isEmpty()) return

        achievements.forEach { habit ->
            val rowBinding = ItemAchievementRowBinding.inflate(
                inflater, binding.achievementsContainer, false
            )
            rowBinding.tvAchievementName.text = habit.name
            rowBinding.tvAchievementTime.text = timeAgo(habit.lastCompletedAt)
            binding.achievementsContainer.addView(rowBinding.root)
        }
    }

    private fun timeAgo(timestampMillis: Long): String {
        val diffMinutes = (System.currentTimeMillis() - timestampMillis) / 60000
        return when {
            diffMinutes < 1 -> "just now"
            diffMinutes < 60 -> "$diffMinutes mins ago"
            diffMinutes < 1440 -> "${diffMinutes / 60}h ago"
            else -> "${diffMinutes / 1440}d ago"
        }
    }

    // ---------- Manage Profile ----------

    private fun showManageProfileDialog() {
        val input = EditText(requireContext()).apply {
            setText(auth.currentUser?.displayName ?: "")
            setSelection(text.length)
        }
        val container = wrapInPaddedContainer(input)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.label_manage_profile))
            .setView(container)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) updateDisplayName(newName)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun updateDisplayName(newName: String) {
        val request = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
        auth.currentUser?.updateProfile(request)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.tvProfileName.text = newName
                    MessageUtils.showSuccess(requireActivity(), "Profile updated")
                } else {
                    MessageUtils.showError(requireActivity(), task.exception?.message ?: "Update failed")
                }
            }
    }

    // ---------- Change Password ----------

    private fun showChangePasswordDialog() {
        val input = EditText(requireContext()).apply {
            hint = getString(R.string.hint_new_password)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val container = wrapInPaddedContainer(input)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.label_change_password))
            .setView(container)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val newPassword = input.text.toString().trim()
                if (newPassword.length < 6) {
                    MessageUtils.showError(requireActivity(), "Password must be at least 6 characters")
                } else {
                    updatePassword(newPassword)
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun updatePassword(newPassword: String) {
        auth.currentUser?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MessageUtils.showSuccess(requireActivity(), "Password changed")
                } else {
                    // Firebase requires a recent login for this sensitive action --
                    // surface that clearly instead of a generic failure message.
                    val message = task.exception?.message ?: "Password change failed"
                    val friendlyMessage = if (message.contains("recent", ignoreCase = true)) {
                        "Please log out and log back in, then try changing your password again"
                    } else message
                    MessageUtils.showError(requireActivity(), friendlyMessage)
                }
            }
    }

    // ---------- Linked Accounts ----------

    private fun showLinkedAccountsDialog() {
        val providers = auth.currentUser?.providerData
            ?.map { readableProviderName(it.providerId) }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?: emptyList()

        val message = if (providers.isEmpty()) {
            "No linked accounts found"
        } else {
            providers.joinToString("\n") { "\u2022 $it" }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.label_manage_linked_accounts))
            .setMessage(message)
            .setPositiveButton(getString(R.string.btn_ok), null)
            .show()
    }

    private fun readableProviderName(providerId: String): String = when (providerId) {
        "password" -> "Email/Password"
        "google.com" -> "Google"
        "yahoo.com" -> "Yahoo"
        "firebase" -> "" // internal, not a real linked provider -- filtered out
        else -> providerId
    }

    // ---------- Reminders ----------

    private fun enableReminders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        ReminderScheduler.enable(requireContext())
        prefs.edit().putBoolean("reminders_enabled", true).apply()
    }

    // ---------- Device token (for testing FCM pushes without USB/Logcat) ----------

    private fun showDeviceTokenDialog() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!isAdded) return@addOnCompleteListener

            if (!task.isSuccessful) {
                MessageUtils.showError(requireActivity(), "Couldn't get device token: ${task.exception?.message}")
                return@addOnCompleteListener
            }

            val token = task.result

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.label_copy_device_token))
                .setMessage(token)
                .setPositiveButton(getString(R.string.btn_copy)) { _, _ ->
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("FCM Token", token)
                    clipboard.setPrimaryClip(clip)
                    MessageUtils.showSuccess(requireActivity(), "Token copied")
                }
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show()
        }
    }

    // ---------- Helpers ----------

    private fun wrapInPaddedContainer(input: EditText): LinearLayout {
        val padding = (20 * resources.displayMetrics.density).toInt()
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, 0)
            addView(input)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}