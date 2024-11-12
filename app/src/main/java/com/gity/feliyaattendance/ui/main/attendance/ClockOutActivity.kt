package com.gity.feliyaattendance.ui.main.attendance

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.data.local.AttendanceDataStoreManager
import com.gity.feliyaattendance.data.local.ProjectDataStoreManager
import com.gity.feliyaattendance.data.model.WorkTimeResult
import com.gity.feliyaattendance.databinding.ActivityClockOutBinding
import com.gity.feliyaattendance.helper.AttendanceManager
import com.gity.feliyaattendance.helper.CloudinaryHelper
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

class ClockOutActivity : AppCompatActivity() {
    private lateinit var attendanceManager: AttendanceManager
    private lateinit var binding: ActivityClockOutBinding
    private var photoUri: Uri? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var repository: Repository
    private lateinit var viewModel: AttendanceViewModel
    private lateinit var cloudinaryHelper: CloudinaryHelper

    companion object {
        const val REQUEST_PERMISSIONS = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClockOutBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        initFirebase()
        setupViewModel()
        setupUI()
        setupCameraAndGalleryLaunchers()
        setupValidationListener()
        displayProjectData()
        handleBackButton()

        cloudinaryHelper = CloudinaryHelper(this@ClockOutActivity)
        val attendanceDataStore = AttendanceDataStoreManager(this)
        val projectDataStore = ProjectDataStoreManager(this)
        attendanceManager =
            AttendanceManager(this, attendanceDataStore, projectDataStore, firebaseFirestore)

        binding.apply {
            btnBack.setOnClickListener { finish() }
            btnClockOut.isEnabled = false
            tvImageUrl.addTextChangedListener {
                validateClockOutButton()
            }
            edtDescriptionProject.addTextChangedListener {
                validateClockOutButton()
            }
            openGalleryOrCamera.setOnClickListener { checkAndRequestPermissions() }
        }
        checkClockInStatus()
    }

    private fun checkClockInStatus() {
        lifecycleScope.launch {
            val attendanceDataStore = AttendanceDataStoreManager(this@ClockOutActivity)
            val hasClockIn = attendanceDataStore.clockIn.firstOrNull() != null

            if (!hasClockIn) {
                binding.apply {
                    btnClockOut.isEnabled = false
                    btnClockOut.alpha = 0.5f
                    // Optional: Show message why button is disabled
                    Toast.makeText(
                        this@ClockOutActivity,
                        "Please clock in first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Even if clocked in, still need to validate other fields
                validateClockOutButton()
            }
        }
    }

    private fun validateClockOutButton() {
        binding.apply {
            // Check all required fields
            val hasImage = !tvImageUrl.text.isNullOrEmpty()
            val hasDescription = !edtDescriptionProject.text.isNullOrEmpty()

            // Enable button only if all conditions are met
            btnClockOut.isEnabled = hasImage && hasDescription

            // Optional: Change button appearance based on state
            btnClockOut.alpha = if (btnClockOut.isEnabled) 1.0f else 0.5f

            btnClockOut.setOnClickListener {
                showConfirmationClockOut()
            }
        }
    }

    private fun showConfirmationClockOut() {
        CommonHelper.showConfirmationDialog(
            context = this@ClockOutActivity,
            title = getString(R.string.title_dialog_confirmation),
            description = getString(R.string.description_dialog_confirmation_default),
            positiveButtonText = getString(R.string.sure),
            negativeButtonText = getString(R.string.cancel),
            onPositiveClick = {
                clockOut()
            }
        )
    }

    // Untuk testing dengan 2 menit sebagai batas regular working time
    private fun calculateRegularAndOvertimeMinutes(totalMinutes: Int): Pair<Int, Int> {
        //val regularTimeLimit = 2 // 2 menit untuk testing
        val regularTimeLimit = 480 // 8 jam
        //val regularTimeLimit = 60 // 1 Jam
        return if (totalMinutes > regularTimeLimit) {
            Pair(regularTimeLimit, totalMinutes - regularTimeLimit)
        } else {
            Pair(totalMinutes, 0)
        }
    }

    private fun calculateWorkTime(clockIn: Timestamp, clockOut: Timestamp): WorkTimeResult {
        val durationMillis = clockOut.toDate().time - clockIn.toDate().time
        val totalMinutes = (durationMillis / (1000 * 60)).toInt() // Konversi ke menit

        return WorkTimeResult(
            hours = totalMinutes / 60,
            minutes = totalMinutes % 60,
            totalMinutes = totalMinutes
        )
    }

    private fun clockOut() {
        lifecycleScope.launch {
            try {
                val attendanceDataStore = AttendanceDataStoreManager(this@ClockOutActivity)
                val clockInTime = attendanceDataStore.clockIn.firstOrNull()
                    ?: throw Exception("Clock in time not found")

                val clockOutTime = Timestamp.now()
                val description = binding.edtDescriptionProject.text.toString()
                val status = "pending"

                val imageUrl = photoUri?.let {
                    cloudinaryHelper.uploadImage(it, firebaseAuth.currentUser?.uid!!)
                } ?: throw Exception("No Image Selected")

                // Hitung waktu kerja
                val workTime = calculateWorkTime(clockInTime, clockOutTime)
                val (regularMinutes, overtimeMinutes) = calculateRegularAndOvertimeMinutes(workTime.totalMinutes)

                attendanceManager.clockOut(
                    clockOut = clockOutTime,
                    imageUrlOut = imageUrl,
                    status = status,
                    workMinutes = regularMinutes,
                    overtimeMinutes = overtimeMinutes,
                    totalMinutes = workTime.totalMinutes,
                    description = description
                )

                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ClockOutActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayProjectData() {
        lifecycleScope.launch {
            val dataStoreManager = ProjectDataStoreManager(this@ClockOutActivity)
            val projectName = dataStoreManager.projectName.firstOrNull()
            val projectLocation = dataStoreManager.projectLocation.firstOrNull()

            binding.apply {
                tvProjectName.text = projectName
                tvLocation.text = projectLocation
            }

        }
    }

    private fun setupValidationListener() {
        binding.apply {
            tvImageUrl.addTextChangedListener { checkFieldsForEmptyValues() }
            edtDescriptionProject.addTextChangedListener { checkFieldsForEmptyValues() }
            checkFieldsForEmptyValues()
        }
    }

    private fun checkFieldsForEmptyValues() {
        val isImageSelected = !binding.tvImageUrl.text.isNullOrEmpty()
        val isDescriptionFilled = !binding.edtDescriptionProject.text.isNullOrEmpty()
        binding.btnClockOut.isEnabled = isImageSelected && isDescriptionFilled
    }

    private fun initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        repository = Repository(firebaseAuth, firebaseFirestore)
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AttendanceViewModel::class.java]
    }

    private fun setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupCameraAndGalleryLaunchers() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && photoUri != null) {
                    Toast.makeText(this, "Picture taken: $photoUri", Toast.LENGTH_SHORT).show()
                    binding.tvImageUrl.text = photoUri.toString()
                    validateClockOutButton()
                } else {
                    Toast.makeText(this, "Failed to take picture", Toast.LENGTH_SHORT).show()
                    binding.tvImageUrl.text = ""
                    validateClockOutButton()
                }
            }

        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    photoUri = uri
                    Toast.makeText(this, "Image selected: $uri", Toast.LENGTH_SHORT).show()
                    binding.tvImageUrl.text = uri.toString()
                    validateClockOutButton()
                } else {
                    Toast.makeText(this, "Failed to pick image", Toast.LENGTH_SHORT).show()
                    binding.tvImageUrl.text = ""
                    validateClockOutButton()
                }
            }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsModern()
        } else {
            requestPermissionsLegacy()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionsModern() {
        if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showDialogImage()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA
                ), REQUEST_PERMISSIONS
            )
        }
    }

    private fun requestPermissionsLegacy() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showDialogImage()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                ), REQUEST_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showDialogImage()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDialogImage() {
        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.custom_dialog_image_and_camera)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_camera).setOnClickListener {
            openCamera()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.linear_layout_gallery).setOnClickListener {
            openGallery()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openCamera() {
        val photoFile = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        takePictureLauncher.launch(photoUri!!)
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun handleBackButton(){
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this@ClockOutActivity) {
            finish()
        }
    }
}
