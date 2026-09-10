package com.the5watermelons.motionpulse.ui.auth

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.databinding.FragmentRegisterBinding
import com.the5watermelons.motionpulse.util.MessageUtils
import com.the5watermelons.motionpulse.util.enablePasswordToggle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.etPassword.enablePasswordToggle()
        binding.etConfirmPassword.enablePasswordToggle()

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordStrength(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnRegister.setOnClickListener { registerUser() }
        binding.tvLogIn.setOnClickListener { findNavController().popBackStack() }
    }

    // ---------- Password strength meter ----------

    private fun updatePasswordStrength(password: String) {
        if (password.isEmpty()) {
            binding.pbPasswordStrength.progress = 0
            binding.tvPasswordStrength.text = ""
            return
        }

        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 10) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        val (progress, colorRes, label) = when {
            score <= 2 -> Triple(33, R.color.mp_error, "Weak")
            score in 3..4 -> Triple(66, R.color.mp_warning, "Medium")
            else -> Triple(100, R.color.mp_success, "Strong")
        }

        val color = ContextCompat.getColor(requireContext(), colorRes)
        binding.pbPasswordStrength.progress = progress
        binding.pbPasswordStrength.progressTintList = ColorStateList.valueOf(color)
        binding.tvPasswordStrength.text = label
        binding.tvPasswordStrength.setTextColor(color)
    }

    // ---------- Registration ----------

    private fun registerUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields")
            return
        }

        if (password.length < 6) {
            showError("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            showError("Passwords don't match")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    updateDisplayName(fullName)
                } else {
                    showError(task.exception?.message ?: "Registration failed")
                }
            }
    }

    private fun updateDisplayName(fullName: String) {
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { onRegisterSuccess() }
            ?: onRegisterSuccess()
    }

    private fun onRegisterSuccess() {
        if (!isAdded) return

        // Sign back out: account is created, but per the flow the user should
        // log in fresh with their new credentials rather than skip straight in.
        auth.signOut()

        MessageUtils.showSuccess(binding.root, "Account created! Please log in.")
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1200)
            if (isAdded) findNavController().popBackStack()
        }
    }

    private fun showError(message: String) {
        if (isAdded) MessageUtils.showError(binding.root, message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}