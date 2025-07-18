package com.gity.feliyaattendance.admin.ui.main.analytics.clustering

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.admin.data.model.AnalysisResult
import com.gity.feliyaattendance.admin.data.model.DateRange
import com.gity.feliyaattendance.admin.data.model.WorkerPerformanceData
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.TensorFlowLiteHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkerAnalysisViewModel (
    private val repository: Repository,
    private val tensorFlowHelper: TensorFlowLiteHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerAnalysisUiState())
    val uiState: StateFlow<WorkerAnalysisUiState> = _uiState.asStateFlow()

    private val TAG = "WorkerAnalysisViewModel"

    fun analyzeWorkerPerformance(dateRange: DateRange) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "Starting analysis for date range: ${dateRange.startDate} to ${dateRange.endDate}")

                // Step 1: Get workers data
                val workersResult = repository.getWorkersData()
                if (workersResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to fetch workers: ${workersResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }

                val workers = workersResult.getOrNull() ?: emptyList()
                Log.d(TAG, "Found ${workers.size} workers")

                if (workers.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No workers found in the system"
                    )
                    return@launch
                }

                // Step 2: Get attendance data
                val attendanceResult = repository.getAttendanceDataForAnalysis(dateRange)
                if (attendanceResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to fetch attendance data: ${attendanceResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }

                val attendanceRecords = attendanceResult.getOrNull() ?: emptyList()
                Log.d(TAG, "Found ${attendanceRecords.size} attendance records")

                if (attendanceRecords.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No attendance records found for the selected date range"
                    )
                    return@launch
                }

                // Step 3: Process performance data
                val processedData = repository.processWorkerPerformanceData(
                    workers, attendanceRecords, dateRange
                )

                Log.d(TAG, "Processed ${processedData.size} worker performance records")

                // Step 4: Run ML prediction
                val analyzedData = tensorFlowHelper.predictBatch(processedData)

                Log.d(TAG, "ML prediction completed")

                // Step 5: Generate summary
                val summary = repository.generatePerformanceSummary(analyzedData)

                // Step 6: Update UI state
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    analysisResult = AnalysisResult(analyzedData, summary),
                    dateRange = dateRange
                )

                Log.d(TAG, "Analysis completed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Analysis failed: ${e.message}"
                )
            }
        }
    }

    fun getWorkersByPerformance(performanceLevel: String): List<WorkerPerformanceData> {
        return _uiState.value.analysisResult?.workers?.let { workers ->
            repository.getWorkersByPerformance(workers, performanceLevel)
        } ?: emptyList()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        tensorFlowHelper.close()
    }
}

data class WorkerAnalysisUiState(
    val isLoading: Boolean = false,
    val analysisResult: AnalysisResult? = null,
    val dateRange: DateRange? = null,
    val error: String? = null
)