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
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.auth.AuthActivity
import com.gity.feliyaattendance.ui.auth.AuthViewModel
import com.gity.feliyaattendance.ui.auth.login.LoginFragment
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RegisterFragment : Fragment() {

    // Gunakan var nullable untuk binding
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var repository: Repository
    private lateinit var viewModel: AuthViewModel

    private lateinit var rolesAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

//        Setup Dropdown Adapter
        rolesAdapter = ArrayAdapter(requireContext(), R.layout.list_item_roles, mutableListOf())

        binding.apply {
//            Setup Dropdown Adapter
            edtRole.setAdapter(rolesAdapter)

            //  Text Watcher untuk menghapus error saat user ngetik
            edtEmail.addTextChangedListener {
                if (edtEmail.text.toString().isNotEmpty()) {
                    edtEmailLayout.error = null
                }
            }

            linearLayoutToLogin.setOnClickListener {
                navigateToLogin()
            }

            btnBack.setOnClickListener {
                navigateToLogin()
            }

            btnRegister.setOnClickListener {
                val email = edtEmail.text.toString()
                val password = edtPassword.text.toString()
                val name = edtName.text.toString()
                val role = edtRole.text.toString()
                if (inputChecker(email, password, name, role)) {
                    viewModel.register(email, password, name, role)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.register_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

        // Observe registration result
        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            result.fold(onSuccess = {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.register_success),
                    Toast.LENGTH_SHORT
                ).show()
                (activity as? AuthActivity)?.replaceFragment(LoginFragment())
            }, onFailure = { exception ->
                Toast.makeText(
                    requireContext(),
                    "${getString(R.string.register_failed)}: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("AUTH", "${getString(R.string.register_failed)}: ${exception.message}")
            })
        }

        //  Observer Roles
        viewModel.roles.observe(viewLifecycleOwner) { result ->
            result.onSuccess { roles ->
                rolesAdapter.clear()
                rolesAdapter.addAll(roles)
                rolesAdapter.notifyDataSetChanged()
            }
            result.onFailure { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load roles: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        //  Trigger Fetching Roles
        viewModel.getRoles()


        return binding.root
    }

    private fun inputChecker(email: String, password: String, name: String, role: String): Boolean {
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

            if (name.isEmpty()) {
                edtName.error = getString(R.string.name_is_empty)
                edtName.requestFocus()
                return false
            }

            if (role.isEmpty()) {
                edtRole.error = getString(R.string.role_is_empty)
                edtRole.requestFocus()
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

    private fun navigateToLogin() {
        (activity as? AuthActivity)?.replaceFragment(LoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding ke null untuk menghindari memory leak
        _binding = null
    }
}
