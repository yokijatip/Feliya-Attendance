package com.gity.feliyaattendance.ui.main.attendance.edit

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.ui.main.detail.attendance.AdminAttendanceDetailViewModel
import com.gity.feliyaattendance.data.local.AttendanceDataStoreManager
import com.gity.feliyaattendance.data.local.ProjectDataStoreManager
import com.gity.feliyaattendance.databinding.ActivityEditAttendanceBinding
import com.gity.feliyaattendance.helper.AttendanceManager
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.gity.feliyaattendance.utils.ViewModelFactoryAttendanceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EditAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAttendanceBinding
    private lateinit var viewModel: AdminAttendanceDetailViewModel
    private lateinit var attendanceManager: AttendanceManager
    private lateinit var viewModelAttendanceManager: EditAttendanceViewModel
    private var attendanceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        setupSwipeRefreshLayout()

        attendanceId = intent.getStringExtra("ATTENDANCE_ID")
        setupViewModel()
        setupViewModelAttendanceManager()
        setupWorkHoursOptionWithTimePickerDialog(attendanceId!!)

        attendanceId?.let {
            viewModel.fetchAttendanceDetail(it)
            observerAttendanceDetail()
        }
    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Reload attendance detail
            attendanceId?.let {
                viewModel.fetchAttendanceDetail(it)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminAttendanceDetailViewModel::class.java]
    }

    private fun setupViewModelAttendanceManager() {
        val attendanceDataStore = AttendanceDataStoreManager(this)
        val projectDataStore = ProjectDataStoreManager(this)
        attendanceManager = AttendanceManager(
            this,
            attendanceDataStore,
            projectDataStore,
            FirebaseFirestore.getInstance()
        )
        val factory = ViewModelFactoryAttendanceManager(attendanceManager)
        viewModelAttendanceManager =
            ViewModelProvider(this, factory)[EditAttendanceViewModel::class.java]
    }

    private fun setupUI() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        handleBackButton()
    }

    private fun setupWorkHoursOptionWithTimePickerDialog(attendanceId: String) {
        binding.tvWorkingHours.setOnClickListener {
            showAdvancedTimePickerDialog(
                title = resources.getString(R.string.choose_working_hours),
                onTimeSelected = { hours, minutes ->
                    val totalMinutes = hours * 60 + minutes
                    viewModelAttendanceManager.updateWorkMinutes(totalMinutes)
                    binding.tvWorkingHours.text = formatTimeDisplay(hours, minutes)
                }
            )
        }

        binding.tvOvertimeHours.setOnClickListener {
            showAdvancedTimePickerDialog(
                title = resources.getString(R.string.choose_overtime_hours),
                onTimeSelected = { hours, minutes ->
                    val totalMinutes = hours * 60 + minutes
                    viewModelAttendanceManager.updateOvertimeMinutes(totalMinutes)
                    binding.tvOvertimeHours.text = formatTimeDisplay(hours, minutes)
                }
            )
        }

        binding.fabSave.setOnClickListener {
            lifecycleScope.launch {
                viewModelAttendanceManager.updateAttendanceHours(attendanceId)
                viewModelAttendanceManager.updateWorkDescription(attendanceId, binding.tvWorkerDescription.text.toString())
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatTimeDisplay(hours: Int, minutes: Int): String {
        return String.format("%d:%02d", hours, minutes)
    }

    private fun showAdvancedTimePickerDialog(
        title: String,
        onTimeSelected: (Int, Int) -> Unit
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.time_picker_dialog_layout)
        dialog.setTitle(title)

        val hoursPicker = dialog.findViewById<NumberPicker>(R.id.hoursPicker)
        val minutesPicker = dialog.findViewById<NumberPicker>(R.id.minutesPicker)
        val btnConfirm = dialog.findViewById<View>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<View>(R.id.btnCancel)

        // Configure Hours Picker
        hoursPicker.minValue = 0
        hoursPicker.maxValue = 12
        hoursPicker.wrapSelectorWheel = true

        // Configure Minutes Picker
        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59
        minutesPicker.wrapSelectorWheel = true

        btnConfirm.setOnClickListener {
            val selectedHours = hoursPicker.value
            val selectedMinutes = minutesPicker.value
            onTimeSelected(selectedHours, selectedMinutes)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observerAttendanceDetail() {
        viewModel.attendanceDetail.observe(this@EditAttendanceActivity) { detail ->
            if (detail != null) {
                binding.apply {

                    //                    Get Value TV WorkDecription
                    viewModelAttendanceManager.workDescription(detail.attendance.workDescription)

                    swipeRefreshLayout.isRefreshing = false
                    tvWorkerName.text = detail.workerName
                    tvProjectName.text = detail.projectName.projectName
                    tvDate.text = CommonHelper.formatTimestamp(detail.attendance.date)
                    tvClockIn.text = CommonHelper.formatTimeOnly(detail.attendance.clockInTime)
                    tvClockOut.text = CommonHelper.formatTimeOnly(detail.attendance.clockOutTime)
                    tvWorkerDescription.setText(detail.attendance.workDescription)
                    tvProjectLocation.text = detail.projectName.location
                    tvTotalHours.text = detail.attendance.getFormattedTotalHours()
                    tvWorkingHours.text = detail.attendance.getFormattedWorkHours()
                    tvOvertimeHours.text = detail.attendance.getFormattedOvertimeHours()


                    val imageList = arrayListOf(
                        SlideModel(detail.attendance.workProofIn, "Clock-In Proof"),
                        SlideModel(detail.attendance.workProofOut, "Clock-Out Proof")
                    )
                    ivDetailAttendance.setImageList(imageList, ScaleTypes.CENTER_CROP)
                }
            } else {
                binding.swipeRefreshLayout.isRefreshing = false
                CommonHelper.showInformationFailedDialog(
                    this@EditAttendanceActivity,
                    getString(R.string.error),
                    getString(R.string.failed)
                )
            }
        }
    }

    private fun handleBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        onBackPressedDispatcher.addCallback {
            finish()
        }
    }
}