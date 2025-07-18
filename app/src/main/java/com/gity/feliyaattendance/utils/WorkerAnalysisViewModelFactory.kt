package com.gity.feliyaattendance.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.admin.ui.main.analytics.clustering.WorkerAnalysisViewModel
import com.gity.feliyaattendance.repository.Repository

class WorkerAnalysisViewModelFactory(
    private val repository: Repository,
    private val tensorFlowHelper: TensorFlowLiteHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerAnalysisViewModel::class.java)) {
            return WorkerAnalysisViewModel(repository, tensorFlowHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}