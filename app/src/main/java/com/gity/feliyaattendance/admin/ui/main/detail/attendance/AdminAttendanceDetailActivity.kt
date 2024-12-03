package com.gity.feliyaattendance.admin.ui.main.detail.attendance

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Window
import android.widget.LinearLayout
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
import com.gity.feliyaattendance.ui.main.attendance.edit.EditAttendanceActivity
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminAttendanceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAttendanceDetailBinding

    private val viewModel: AdminAttendanceDetailViewModel by viewModels {
        ViewModelFactory(Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()))
    }

    private var attendanceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminAttendanceDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupWindowInsets()
        attendanceId = intent.getStringExtra("ATTENDANCE_ID")
        setupListeners(attendanceId!!)

        attendanceId?.let { id ->
            viewModel.fetchAttendanceDetail(id)
            observerAttendanceDetail()
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
                    tvProjectLocation.text = detail.projectName.location
                    tvTotalHours.text = detail.attendance.getFormattedTotalHours()
                    tvWorkingHours.text = detail.attendance.getFormattedWorkHours()
                    tvOvertimeHours.text = detail.attendance.getFormattedOvertimeHours()

                    val imageList = arrayListOf(
                        SlideModel(detail.attendance.workProofIn, "Clock-In Proof"),
                        SlideModel(detail.attendance.workProofOut, "Clock-Out Proof")
                    )
                    ivDetailAttendance.setImageList(imageList, ScaleTypes.CENTER_CROP)
                    updateStatusUI(detail.attendance.status)
                }
            } else {
                CommonHelper.showInformationFailedDialog(
                    this@AdminAttendanceDetailActivity,
                    getString(R.string.error),
                    getString(R.string.failed)
                )
            }
        }
    }

//    // Observasi hasil update status
//    private fun observeUpdateStatus() {
//        viewModel.updateStatusSuccess.observe(this) { isSuccess ->
//            if (isSuccess) {
//                Toast.makeText(this, "Attendance status updated", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Error updating attendance status", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

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

    private fun setupListeners(attendanceId: String) {
        binding.apply {
            btnBack.setOnClickListener { finish() }
            fabApproved.setOnClickListener {
                attendanceId.let {
                    viewModel.updateAttendanceStatus(it, approveAttendance())
                    updateStatusUI(approveAttendance())
                }
            }
        }
        moreMenu(attendanceId)
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
                        swipeRefreshLayout.isRefreshing = false
                    }
                }

            }
        }
    }

    private fun approveAttendance(): String {
        val approve = "approved"
        return approve
    }

    private fun rejectAttendance() {
        val rejectedStatus = "rejected"
        attendanceId?.let {
            viewModel.updateAttendanceStatus(it, rejectedStatus)
            updateStatusUI(rejectedStatus)
        }
    }

    private fun pendingAttendance() {
        val pendingStatus = "pending"
        attendanceId?.let {
            viewModel.updateAttendanceStatus(it, pendingStatus)
            updateStatusUI(pendingStatus)
        }
    }

    private fun deleteAttendance() {
        attendanceId?.let {
            viewModel.deleteAttendance(it)
            finish()
        }
    }

    private fun moreMenu(attendanceId: String) {
        binding.apply {
            btnMore.setOnClickListener {
                showDialogImage(attendanceId)
            }
        }
    }

    private fun showDialogImage(attendanceId: String) {
        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.custom_dialog_menu_admin_attendance_detail)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_pending).setOnClickListener {
            pendingAttendance()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.linear_layout_rejected).setOnClickListener {
            rejectAttendance()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_edit_attendance).setOnClickListener {
            //editAttendance()
            navigateToEditAttendance(attendanceId)
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_delete).setOnClickListener {
            //deleteAttendance()
            CommonHelper.showConfirmationDialog(
                this@AdminAttendanceDetailActivity,
                getString(R.string.confirmation_delete_attendance),
                getString(R.string.confirmation_delete_attendance_description),
                onPositiveClick = { deleteAttendance() },
                onNegativeClick = { dialog.dismiss() }
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun navigateToEditAttendance(attendanceId: String) {
        val intent = Intent(this, EditAttendanceActivity::class.java)
        intent.putExtra("ATTENDANCE_ID", attendanceId)
        startActivity(intent)
    }
}