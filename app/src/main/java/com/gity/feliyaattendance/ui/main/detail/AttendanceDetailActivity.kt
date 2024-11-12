package com.gity.feliyaattendance.ui.main.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAttendanceBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AttendanceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private var attendanceId: String? = null

    private val viewModel: AttendanceDetailViewModel by viewModels {
        ViewModelFactory(Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        attendanceId = intent.getStringExtra("ATTENDANCE_ID")

        attendanceId?.let { id ->
            viewModel.fetchAttendanceDetail(id)
            observerAttendanceDetail()
        }

        setupUI()
    }

    private fun observerAttendanceDetail() {
        viewModel.attendanceDetail.observe(this@AttendanceDetailActivity) { detail ->
            if (detail != null) {
                binding.apply {
                    tvWorkerName.text = detail.workerName
                    tvProjectName.text = detail.projectName.projectName
                    tvDate.text = CommonHelper.formatTimestamp(detail.attendance.date)
                    tvClockIn.text = CommonHelper.formatTimeOnly(detail.attendance.clockInTime)
                    tvClockOut.text = CommonHelper.formatTimeOnly(detail.attendance.clockOutTime)
                    tvWorkerDescription.text = detail.attendance.workDescription
                    tvProjectLocation.text = detail.projectName.location
                    tvTotalHours.text = detail.attendance.getFormattedTotalHours()
                    tvWorkingHours.text = detail.attendance.getFormattedWorkHours()
                    tvOvertimeHours.text = detail.attendance.getFormattedOvertimeHours()
                    val imageList = arrayListOf(
                        SlideModel(detail.attendance.workProofIn, getString(R.string.clock_in_proof)),
                        SlideModel(detail.attendance.workProofOut, getString(R.string.clock_out_proof))
                    )
                    ivDetailAttendance.setImageList(imageList, ScaleTypes.CENTER_CROP)
                    updateStatusUI(detail.attendance.status)
                }
            } else {
                showToast("Attendance detail not found")
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
                    ContextCompat.getColor(this@AttendanceDetailActivity, backgroundColor)
                )
            )

            // Update TextView color and text
            tvStatus.apply {
                setTextColor(
                    ContextCompat.getColor(
                        this@AttendanceDetailActivity,
                        textColor
                    )
                )
                text = status
            }
        }
    }

    private fun setupUI() {
        setupWindowInsets()
        setupListeners()
        attendanceId?.let { swipeRefreshLayout(it) }
    }

    private fun setupWindowInsets() {
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
                viewModel.attendanceDetail.observe(this@AttendanceDetailActivity) { detail ->
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

    private fun setupListeners() {
        binding.apply {
            btnBack.setOnClickListener { finish() }
        }
        onBackPressedDispatcher.addCallback {
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}