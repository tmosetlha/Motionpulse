package com.the5watermelons.motionpulse.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.the5watermelons.motionpulse.util.MessageUtils
import com.the5watermelons.motionpulse.util.enablePasswordToggle
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

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

        binding.etPassword.enablePasswordToggle()

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
            showError("Please enter your email and password")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    onLoginSuccess()
                } else {
                    showError(task.exception?.message ?: "Login failed")
                }
            }
    }

    private fun sendPasswordReset() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            showError("Enter your email above first")
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MessageUtils.showSuccess(requireActivity(), "Password reset email sent")
                } else {
                    showError("Couldn't send reset email: ${task.exception?.message}")
                }
            }
    }

    // ---------- Google (Credential Manager) ----------

    private fun signInWithGoogle() {
        // Immediate feedback so tapping the button is never silent -- if this toast
        // doesn't appear, the tap itself isn't registering (a UI/click-wiring issue).
        // If it appears but nothing follows, the hang is inside Credential Manager
        // itself (almost always missing Play Services / no Play Store on this device).
        android.widget.Toast.makeText(requireContext(), "Opening Google Sign-In...", android.widget.Toast.LENGTH_SHORT).show()

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
                val result = withTimeout(8000L) {
                    credentialManager.getCredential(requireActivity(), request)
                }
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
                            if (task.isSuccessful) onLoginSuccess()
                            else showError(task.exception?.message ?: "Google sign-in failed")
                        }
                } else {
                    showError("Unexpected credential type from Google")
                }
            } catch (e: TimeoutCancellationException) {
                showError("Google Sign-In timed out. This usually means this device/emulator doesn't have Google Play Services or the Play Store app available.")
            } catch (e: GetCredentialException) {
                // Surface the exact exception type (e.g. NoCredentialException usually
                // means no Google account on device, or Play Services/Play Store missing
                // on the emulator) so real failures are diagnosable instead of generic.
                showError("Google sign-in failed [${e.javaClass.simpleName}]: ${e.message}")
            } catch (e: Exception) {
                showError("Google sign-in error [${e.javaClass.simpleName}]: ${e.message}")
            }
        }
    }

    // ---------- Yahoo (Firebase generic OAuth) ----------

    private fun signInWithYahoo() {
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending
                .addOnSuccessListener { onLoginSuccess() }
                .addOnFailureListener { showError(it.message ?: "Yahoo sign-in failed") }
            return
        }

        val provider = OAuthProvider.newBuilder("yahoo.com")
        auth.startActivityForSignInWithProvider(requireActivity(), provider.build())
            .addOnSuccessListener { onLoginSuccess() }
            .addOnFailureListener { showError(it.message ?: "Yahoo sign-in failed") }
    }

    // ---------- Helpers ----------

    private fun onLoginSuccess() {
        if (!isAdded) return
        MessageUtils.showSuccess(requireActivity(), "Welcome back!")
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            if (isAdded) {
                findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
            }
        }
    }

    private fun showError(message: String) {
        if (isAdded) MessageUtils.showError(requireActivity(), message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}