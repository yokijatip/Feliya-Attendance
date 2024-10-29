package com.gity.feliyaattendance.admin.ui.main.workers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.FragmentAdminProjectsBinding
import com.gity.feliyaattendance.databinding.FragmentAdminWorkerBinding


class AdminWorkersFragment : Fragment() {

    private var _binding: FragmentAdminWorkerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdminWorkerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}