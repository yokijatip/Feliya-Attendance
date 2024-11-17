package com.gity.feliyaattendance.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.ui.main.MainAdminActivity
import com.gity.feliyaattendance.databinding.FragmentLoginBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.auth.AuthActivity
import com.gity.feliyaattendance.ui.auth.AuthViewModel
import com.gity.feliyaattendance.ui.auth.register.RegisterFragment
import com.gity.feliyaattendance.ui.main.MainActivity
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var repository: Repository
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupClickListeners()
        observeViewModel()

        return binding.root
    }

    private fun setupClickListeners() {
        binding.apply {
            createNewAccount.setOnClickListener {
                (activity as? AuthActivity)?.replaceFragment(RegisterFragment())
            }

            orangeLine.setOnClickListener {
                edtEmail.setText(CommonHelper.generateRandomEmail())
                edtPassword.setText(CommonHelper.generateRandomPassword())
            }

            btnLogin.setOnClickListener {
                handleLoginAttempt()
            }

            // Text Watcher to clear error when user types
            edtEmail.addTextChangedListener {
                if (edtEmail.text.toString().isNotEmpty()) {
                    edtEmailLayout.error = null
                }
            }
        }
    }

    private fun handleLoginAttempt() {
        showLoading(true)
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPassword.text.toString()

        if (inputChecker(email, password)) {
            checkAccountStatusAndLogin(email, password)
        } else {
            showLoading(false)
        }
    }

    private fun checkAccountStatusAndLogin(email: String, password: String) {
        firebaseFirestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    showLoading(false)
                    CommonHelper.showInformationFailedDialog(
                        requireContext(),
                        getString(R.string.error),
                        getString(R.string.user_not_found)
                    )
                    return@addOnSuccessListener
                }

                val userDoc = documents.documents[0]
                when (userDoc.getString("status")?.lowercase()) {
                    "activated" -> {
                        viewModel.login(email, password)
                    }

                    "pending" -> {
                        showLoading(false)
                        CommonHelper.showInformationFailedDialog(
                            requireContext(),
                            getString(R.string.failed),
                            getString(R.string.account_pending_message)
                        )
                    }

                    "suspended" -> {
                        showLoading(false)
                        CommonHelper.showInformationFailedDialog(
                            requireContext(),
                            getString(R.string.failed),
                            getString(R.string.account_suspended_message)
                        )
                    }

                    else -> {
                        showLoading(false)
                        Toast.makeText(
                            requireContext(),
                            "Invalid account status. Please contact admin.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                CommonHelper.showInformationFailedDialog(
                    requireContext(),
                    getString(R.string.failed),
                    "${getString(R.string.error_checking_account_status)} ${e.message}"
                )
                Log.e("AUTH", "Error checking account status: ${e.message}")
            }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { role ->
                    showLoading(false)
                    when (role) {
                        "worker" -> navigateToMain()
                        "admin" -> navigateToAdmin()
                    }
                },
                onFailure = { e ->
                    showLoading(false)
                    CommonHelper.showInformationFailedDialog(
                        requireContext(),
                        getString(R.string.error),
                        "${getString(R.string.login_failed)} ${e.message}"
                    )
                    Log.e("AUTH", "Login Failed: ${e.message}")
                }
            )
        }
    }

    private fun inputChecker(email: String, password: String): Boolean {
        binding.apply {
            if (email.isEmpty()) {
                edtEmail.error = getString(R.string.email_is_empty)
                edtEmailLayout.requestFocus()
                return false
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.error = requireContext().getString(R.string.email_is_not_valid)
                edtEmail.requestFocus()
                return false
            }

            if (password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.password_is_empty),
                    Toast.LENGTH_SHORT
                ).show()
                edtPassword.error = getString(R.string.password_is_empty)
                edtPassword.requestFocus()
                return false
            }

            if (password.length < 8) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.password_helper),
                    Toast.LENGTH_SHORT
                ).show()
                edtPassword.error = getString(R.string.password_helper)
                edtPassword.requestFocus()
                return false
            }
        }
        return true
    }

    private fun navigateToMain() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToAdmin() {
        val intent = Intent(requireActivity(), MainAdminActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}