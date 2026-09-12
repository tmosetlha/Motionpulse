package com.the5watermelons.motionpulse.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Your Profile"
        binding.tvProfileName.text = displayName

        binding.btnLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // Navigate on the OUTER nav controller (nav_host_fragment), not the
            // nested bottom-nav one, since Login lives outside the bottom nav shell.
            val outerNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
            outerNavController.navigate(R.id.action_mainFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}