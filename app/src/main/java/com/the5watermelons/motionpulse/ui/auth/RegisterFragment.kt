package com.the5watermelons.motionpulse.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.databinding.FragmentRegisterBinding

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

        binding.btnRegister.setOnClickListener { registerUser() }
        binding.tvLogIn.setOnClickListener { findNavController().popBackStack() }
    }

    private fun registerUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            toast("Please fill in all fields")
            return
        }

        if (password.length < 6) {
            toast("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            toast("Passwords don't match")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    updateDisplayName(fullName)
                } else {
                    toast(task.exception?.message ?: "Registration failed")
                }
            }
    }

    private fun updateDisplayName(fullName: String) {
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener {
                // Whether or not the display name update succeeds, the account
                // itself was already created successfully -- proceed to Home.
                goToHome()
            }
            ?: goToHome()
    }

    private fun goToHome() {
        if (isAdded) {
            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
        }
    }

    private fun toast(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}