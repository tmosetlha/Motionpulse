package com.the5watermelons.motionpulse.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener { signInWithEmail() }
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }
        binding.btnYahoo.setOnClickListener { signInWithYahoo() }
        binding.tvForgotPassword.setOnClickListener { sendPasswordReset() }
        binding.tvCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    // ---------- Email / Password ----------

    private fun signInWithEmail() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            toast("Please enter your email and password")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    goToHome()
                } else {
                    toast(task.exception?.message ?: "Login failed")
                }
            }
    }

    private fun sendPasswordReset() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            toast("Enter your email above first")
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                toast(
                    if (task.isSuccessful) "Password reset email sent"
                    else "Couldn't send reset email: ${task.exception?.message}"
                )
            }
    }

    // ---------- Google (Credential Manager) ----------

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(requireContext())

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(requireActivity(), request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken, null
                    )
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) goToHome()
                            else toast(task.exception?.message ?: "Google sign-in failed")
                        }
                } else {
                    toast("Unexpected credential type from Google")
                }
            } catch (e: GetCredentialException) {
                toast(e.message ?: "Google sign-in was cancelled or failed")
            }
        }
    }

    // ---------- Yahoo (Firebase generic OAuth) ----------

    private fun signInWithYahoo() {
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending
                .addOnSuccessListener { goToHome() }
                .addOnFailureListener { toast(it.message ?: "Yahoo sign-in failed") }
            return
        }

        val provider = OAuthProvider.newBuilder("yahoo.com")
        auth.startActivityForSignInWithProvider(requireActivity(), provider.build())
            .addOnSuccessListener { goToHome() }
            .addOnFailureListener { toast(it.message ?: "Yahoo sign-in failed") }
    }

    // ---------- Helpers ----------

    private fun goToHome() {
        if (isAdded) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
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