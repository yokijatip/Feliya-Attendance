package com.gity.feliyaattendance.ui.auth.register

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
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

        binding.apply {
            //  Text Watcher untuk menghapus error saat user ngetik
            edtEmail.addTextChangedListener {
                if (edtEmail.text.toString().isNotEmpty()) {
                    edtEmailLayout.error = null
                }
            }

            btnBack.setOnClickListener {
                (activity as? AuthActivity)?.replaceFragment(LoginFragment())
            }

            btnRegister.setOnClickListener {
                val email = edtEmail.text.toString()
                val password = edtPassword.text.toString()
                if (inputChecker(email, password)) {
                    viewModel.register(email, password)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.register_failed), Toast.LENGTH_SHORT).show()
                }
            }

        }

        // Observe registration result
        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            result.fold(onSuccess = {
                Toast.makeText(requireContext(), getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                (activity as? AuthActivity)?.replaceFragment(LoginFragment())
            }, onFailure = { exception ->
                Toast.makeText(requireContext(), "${getString(R.string.register_failed)}: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("AUTH_REGISTER", "${getString(R.string.register_failed)}: ${exception.message}")
            })
        }


        return binding.root
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding ke null untuk menghindari memory leak
        _binding = null
    }
}
