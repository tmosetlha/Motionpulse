package com.the5watermelons.motionpulse.ui.splash

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.databinding.FragmentSplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup the local video file resource path securely
        val videoPath = "android.resource://" + requireContext().packageName + "/" + R.raw.motion_pulser_video
        val videoUri = Uri.parse(videoPath)
        
        binding.introVideoView.setVideoURI(videoUri)
        
        // Start playback as soon as the video is buffered and ready
        binding.introVideoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
            binding.introVideoView.start()
        }

        // The moment the video finishes, trigger the visible transition
        binding.introVideoView.setOnCompletionListener {
            executeFadeTransitionSequence()
        }

        // Safety fallback: if video fails to load, jump straight to the splash content
        binding.introVideoView.setOnErrorListener { _, _, _ ->
            executeFadeTransitionSequence()
            true
        }
    }

    private fun executeFadeTransitionSequence() {
        if (_binding == null) return

        // 1. Fade OUT the video player (Visible Transition)
        binding.introVideoView.animate()
            .alpha(0f)
            .setDuration(FADE_DURATION_MS)
            .withEndAction {
                binding.introVideoView.visibility = View.GONE
            }
            .start()

        // 2. Fade IN the splash logo and loading bar
        binding.splashContentContainer.animate()
            .alpha(1f)
            .setDuration(FADE_DURATION_MS)
            .withEndAction {
                // Keep the splash elements visible for a moment before moving to Login
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(POST_TRANSITION_DELAY_MS)
                    if (isAdded) {
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                }
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val FADE_DURATION_MS = 600L
        private const val POST_TRANSITION_DELAY_MS = 1500L
    }
}