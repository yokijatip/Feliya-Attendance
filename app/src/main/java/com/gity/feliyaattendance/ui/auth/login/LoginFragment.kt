package com.gity.feliyaattendance.ui.auth.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.FragmentLoginBinding
import com.gity.feliyaattendance.databinding.FragmentRegisterBinding


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
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding ke null untuk menghindari memory leak
        _binding = null
    }
}