package com.gity.feliyaattendance.admin.ui.main.detail.attendance

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAdminAttendanceDetailBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminAttendanceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAttendanceDetailBinding

    private val viewModel: AdminAttendanceDetailViewModel by viewModels {
        ViewModelFactory(Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()))
    }


    //    Status Attendance Adapter
    private lateinit var statusAttendanceAdapter: ArrayAdapter<String>
    private var attendanceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminAttendanceDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupWindowInsets()
        setupListeners()
        setupStatusAdapter()


        attendanceId = intent.getStringExtra("ATTENDANCE_ID")

        attendanceId?.let { id ->
            viewModel.fetchAttendanceDetail(id)
            observerAttendanceDetail()
            setupSaveButton()
        }
        attendanceId?.let { swipeRefreshLayout(it) }
    }

    private fun observerAttendanceDetail() {
        viewModel.attendanceDetail.observe(this@AdminAttendanceDetailActivity) { detail ->
            if (detail != null) {
                binding.apply {
                    tvWorkerName.text = detail.workerName
                    tvProjectName.text = detail.projectName.projectName
                    tvDate.text = CommonHelper.formatTimestamp(detail.attendance.date)
                    tvClockIn.text = CommonHelper.formatTimeOnly(detail.attendance.clockInTime)
                    tvClockOut.text = CommonHelper.formatTimeOnly(detail.attendance.clockOutTime)
                    tvWorkerDescription.text = detail.attendance.workDescription
                    tvProjectStartDate.text = CommonHelper.formatTimestamp(detail.projectName.startDate)
                    tvProjectEndDate.text = CommonHelper.formatTimestamp(detail.projectName.endDate)
                    tvProjectLocation.text = detail.projectName.location
                    tvTotalHours.text = detail.attendance.getFormattedTotalHours()
                    tvWorkingHours.text = detail.attendance.getFormattedWorkHours()
                    tvOvertimeHours.text= detail.attendance.getFormattedOvertimeHours()

                    val imageList = arrayListOf(
                        SlideModel(detail.attendance.workProofIn, "Clock-In Proof"),
                        SlideModel(detail.attendance.workProofOut, "Clock-Out Proof")
                    )
                    ivDetailAttendance.setImageList(imageList, ScaleTypes.CENTER_CROP)
                    updateStatusUI(detail.attendance.status)
                }
            } else {
                showToast("Attendance detail not found")
            }
        }
    }

    private fun setupSaveButton() {
        binding.apply {
            // Disable button initially
            btnSave.isEnabled = false

            // Set up dropdown item selection listener
            edtStatus.setOnItemClickListener { _, _, _, _ ->
                // Enable button if dropdown has a selected value
                btnSave.isEnabled = !edtStatus.text.isNullOrEmpty()
            }

            btnSave.setOnClickListener {
                val newStatus = edtStatus.text.toString()
                attendanceId?.let {
                    viewModel.updateAttendanceStatus(it, newStatus)
                    observeUpdateStatus()
                }
            }
        }
    }


    // Observasi hasil update status
    private fun observeUpdateStatus() {
        viewModel.updateStatusSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Attendance status updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error updating attendance status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatusUI(status: String? = null) {
        val (backgroundColor, textColor) = when (status) {
            "pending" -> Pair(
                R.color.status_pending_background,
                R.color.status_pending
            )

            "rejected" -> Pair(
                R.color.status_rejected_background,
                R.color.status_rejected
            )

            "approved" -> Pair(
                R.color.status_approved_background,
                R.color.status_approved
            )

            else -> Pair(
                R.color.status_pending_background,
                R.color.status_pending
            )
        }

        binding.apply {
            // Update CardView background
            cardStatus.setBackgroundTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(this@AdminAttendanceDetailActivity, backgroundColor)
                )
            )

            // Update TextView color and text
            tvStatus.apply {
                setTextColor(
                    ContextCompat.getColor(
                        this@AdminAttendanceDetailActivity,
                        textColor
                    )
                )
                text = status
            }
        }
    }

    private fun setupStatusAdapter() {
        statusAttendanceAdapter = ArrayAdapter(
            this,
            R.layout.list_item_roles,
            resources.getStringArray(R.array.status_attendance).toMutableList()
        )

        binding.edtStatus.apply {
            setAdapter(statusAttendanceAdapter)
            // Menambahkan listener untuk perubahan status
            setOnItemClickListener { _, _, _, _ ->
                val selectedStatus = text.toString()
                updateStatusUI(selectedStatus)
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnBack.setOnClickListener { finish() }

            btnSave.setOnClickListener {
                val statusText = edtStatus.text.toString()
                updateStatusUI(statusText)
                showToast("Status updated to: $statusText")
            }
        }
    }

    private fun setupWindowInsets() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun swipeRefreshLayout(attendanceId: String) {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.fetchAttendanceDetail(attendanceId)
                viewModel.attendanceDetail.observe(this@AdminAttendanceDetailActivity) { detail ->
                    if (detail != null) {
                        updateStatusUI(detail.attendance.status)
                        swipeRefreshLayout.isRefreshing = false
                    } else {
                        showToast("Attendance detail not found")
                        swipeRefreshLayout.isRefreshing = false
                    }
                }

            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}