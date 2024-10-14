package com.gity.feliyaattendance.ui.main.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.FragmentSettingsBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    private val viewModel: SettingsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.apply {
            btnLogout.setOnClickListener {
                showConfirmationDialog()
            }
        }

        return binding.root
    }

    private fun showConfirmationDialog() {
        CommonHelper.showConfirmationDialog(
            context = requireActivity(),
            title = getString(R.string.title_dialog_confirmation),
            description = getString(R.string.logout_confirmation),
            positiveButtonText = getString(R.string.sure),
            negativeButtonText = getString(R.string.cancel),
            onPositiveClick = { logoutUser() },
            onNegativeClick = { }
        )
    }

    private fun logoutUser() {
        firebaseAuth.signOut()

//        Navigate to AuthScreen
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}