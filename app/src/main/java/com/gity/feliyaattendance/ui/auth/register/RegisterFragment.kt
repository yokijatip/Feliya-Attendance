package com.gity.feliyaattendance.ui.auth.register

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.FragmentRegisterBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.auth.AuthActivity
import com.gity.feliyaattendance.ui.auth.AuthViewModel
import com.gity.feliyaattendance.ui.auth.login.LoginFragment
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var repository: Repository
    private lateinit var viewModel: AuthViewModel
    private lateinit var rolesAdapter: ArrayAdapter<String>

    companion object {
        private const val TAG = "RegisterFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        setupFirebase()
        setupViewModel()
        setupViews()
        setupObservers()
        return binding.root
    }

    private fun setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        repository = Repository(firebaseAuth, firebaseFirestore)
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun setupViews() {
        setupRolesAdapter()
        setupClickListeners()
        setupTextWatchers()
    }

    private fun setupRolesAdapter() {
        rolesAdapter = ArrayAdapter(requireContext(), R.layout.list_item_roles, mutableListOf())
        binding.edtRole.setAdapter(rolesAdapter)
    }

    private fun setupClickListeners() {
        binding.apply {
            linearLayoutToLogin.setOnClickListener { navigateToLogin() }
            btnBack.setOnClickListener { navigateToLogin() }
            btnRegister.setOnClickListener { handleRegistration() }
        }
    }

    private fun setupTextWatchers() {
        binding.apply {
            edtEmail.addTextChangedListener {
                if (edtEmail.text.toString().isNotEmpty()) {
                    edtEmailLayout.error = null
                }
            }
        }
    }

    private fun setupObservers() {
        observeRegistrationResult()
        observeRoles()
        viewModel.getRoles() // Trigger fetching roles
    }

    private fun observeRegistrationResult() {
        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    showLoading(false)
                    navigateToLogin()
                },
                onFailure = { exception ->
                    showLoading(false)
                    val errorMessage =
                        "${getString(R.string.register_failed)}: ${exception.message}"
                    CommonHelper.showInformationFailedDialog(
                        requireContext(),
                        getString(R.string.register_failed),
                        "Error : ${exception.message}"
                    )
                    Log.e(TAG, errorMessage)
                }
            )
        }
    }

    private fun observeRoles() {
        viewModel.roles.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { roles ->
                    updateRolesAdapter(roles)
                },
                onFailure = { exception ->
                    showToast("Failed to load roles: ${exception.message}")
                }
            )
        }
    }

    private fun updateRolesAdapter(roles: List<String>) {
        rolesAdapter.apply {
            clear()
            addAll(roles)
            notifyDataSetChanged()
        }
    }

    private fun handleRegistration() {
        showLoading(true)
        binding.apply {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            val name = edtName.text.toString()
            val role = edtRole.text.toString()

            if (validateInputs(email, password, name, role)) {
                // Register with default status
                viewModel.register(
                    email = email,
                    password = password,
                    name = name,
                    role = role
                )
            } else {
                showLoading(false)
            }
        }
    }

    private fun validateInputs(
        email: String,
        password: String,
        name: String,
        role: String
    ): Boolean {
        binding.apply {
            when {
                email.isEmpty() -> {
                    edtEmail.error = getString(R.string.email_is_empty)
                    edtEmailLayout.requestFocus()
                    return false
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    edtEmail.error = getString(R.string.email_is_not_valid)
                    edtEmail.requestFocus()
                    return false
                }

                name.isEmpty() -> {
                    edtName.error = getString(R.string.name_is_empty)
                    edtName.requestFocus()
                    return false
                }

                role.isEmpty() -> {
                    edtRole.error = getString(R.string.role_is_empty)
                    edtRole.requestFocus()
                    return false
                }

                password.isEmpty() -> {
                    showToast(getString(R.string.password_is_empty))
                    edtPassword.error = getString(R.string.password_is_empty)
                    edtPassword.requestFocus()
                    return false
                }

                password.length < 8 -> {
                    showToast(getString(R.string.password_helper))
                    edtPassword.error = getString(R.string.password_helper)
                    edtPassword.requestFocus()
                    return false
                }
            }
            return true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        (activity as? AuthActivity)?.replaceFragment(LoginFragment())
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegister.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}