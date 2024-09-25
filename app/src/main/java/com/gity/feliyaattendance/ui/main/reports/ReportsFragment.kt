package com.gity.feliyaattendance.ui.main.reports

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.FragmentRegisterBinding

class ReportsFragment : Fragment() {
    companion object {
        fun newInstance() = ReportsFragment()
    }

    private val viewModel: ReportsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }


}