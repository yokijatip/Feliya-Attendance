package com.gity.feliyaattendance.ui.main.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.adapter.SettingAdapter
import com.gity.feliyaattendance.data.model.Setting
import com.gity.feliyaattendance.databinding.FragmentSettingsBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.ui.auth.AuthActivity
import com.gity.feliyaattendance.ui.main.settings.account.AccountActivity
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        settingListSetup()
    }

    private fun settingListSetup() {
        val settingList = listOf(
            Setting(getString(R.string.account), R.drawable.ic_people_tag),
            Setting(getString(R.string.help), R.drawable.ic_headset_help),
            Setting(getString(R.string.logout), R.drawable.ic_log_out)
        )

        val settingAdapter = SettingAdapter(settingList) { setting ->
            when (setting.tvSetting) {
                getString(R.string.account) -> {
                    // Handle account click
//                    Toast.makeText(requireContext(), "Account clicked", Toast.LENGTH_SHORT).show()
                    navigateToAccount()
                }

                getString(R.string.help) -> {
                    // Handle help click
                    Toast.makeText(requireContext(), "Help clicked", Toast.LENGTH_SHORT).show()
                }

                getString(R.string.logout) -> {
                    showConfirmationDialog()
                }

                else -> {
                    // Handle other cases if needed
                    Log.e("SettingFragment", "Unknown setting clicked: ${setting.tvSetting}")
                }
            }
        }

        binding.rvSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingAdapter
        }


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

    private fun navigateToAccount() {
        binding.apply {
            val intent = Intent(requireContext(), AccountActivity::class.java)
            startActivity(intent)
        }
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