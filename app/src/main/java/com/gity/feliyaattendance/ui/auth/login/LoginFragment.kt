package com.gity.feliyaattendance.ui.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    // Gunakan var nullable untuk binding
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.apply {
            btnLogin.setOnClickListener {
                //  Text Watcher untuk menghapus error saat user ngetik
                edtWorkerId.addTextChangedListener {
                    if (edtWorkerId.text.toString().isNotEmpty()) {
                        edtWorkerIdLayout.error = null
                    }
                }

                val workerIdInput = edtWorkerId.text.toString()
                val password = edtPassword.text.toString()
                if (inputChecker(workerIdInput, password)) {
                    Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    private fun inputChecker(workerIdInput: String, password: String): Boolean{
        binding.apply {
            if (workerIdInput.isEmpty()) {
                edtWorkerIdLayout.error = getString(R.string.worker_id_is_empty)
                edtWorkerIdLayout.requestFocus()
                return false
            }

            if (password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.password_is_empty), Toast.LENGTH_SHORT).show()
                binding.edtPassword.requestFocus()
                return false
            }

            if (password.length < 8) {
                Toast.makeText(requireContext(), getString(R.string.password_helper), Toast.LENGTH_SHORT).show()
                binding.edtPassword.requestFocus()
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