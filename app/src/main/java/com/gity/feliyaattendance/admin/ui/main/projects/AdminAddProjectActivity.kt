package com.gity.feliyaattendance.admin.ui.main.projects

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAdminAddProjectBinding
import com.gity.feliyaattendance.helper.CloudinaryHelper
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.main.attendance.ClockOutActivity
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AdminAddProjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAddProjectBinding
    private lateinit var statusProjectAdapter: ArrayAdapter<String>
    private var startDate: Date? = null
    private var endDate: Date? = null
    private val calendar = Calendar.getInstance()
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private lateinit var viewModel: AdminProjectViewModel
    private lateinit var repository: Repository

    private lateinit var cloudinaryHelper: CloudinaryHelper

    private var photoUri: Uri? = null

    companion object {
        const val REQUEST_PERMISSIONS = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminProjectViewModel::class.java]

        setupUI()
        setupStatusProjectDropdown()
        setupListeners()
        setupCameraAndGalleryLaunchers()
        observeViewModel()

        binding.apply {
            openGalleryOrCamera.setOnClickListener {
                checkAndRequestPermissions()
            }

            linearLayoutOpenGalleryOrCamera.setOnClickListener {
                checkAndRequestPermissions()
            }
        }
    }

    private fun showConfirmationAddProject() {
        CommonHelper.showConfirmationDialog(
            context = this@AdminAddProjectActivity,
            title = getString(R.string.title_dialog_confirmation),
            description = getString(R.string.description_dialog_confirmation_default),
            positiveButtonText = getString(R.string.sure),
            negativeButtonText = getString(R.string.cancel),
            onPositiveClick = {
                addProject()
            }
        )
    }

    private fun observeViewModel() {
        viewModel.saveProjectResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Project added successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity after successful save
            }
            result.onFailure { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun setupStatusProjectDropdown() {
        statusProjectAdapter =
            ArrayAdapter(this, R.layout.list_item_roles, setupStatusProject())
        binding.edtStatusProject.setAdapter(statusProjectAdapter)
    }

    private fun setupStatusProject(): List<String> {
        return listOf(
            getString(R.string.status_project_active),
            getString(R.string.status_project_inactive),
            getString(R.string.status_project_completed)
        )
    }

    private fun setupListeners() {
        binding.apply {
            btnStartDate.setOnClickListener {
                showDatePicker { date ->
                    binding.tvStartDate.text =
                        SimpleDateFormat("dd - MMMM - yyyy", Locale.getDefault()).format(date)
                    startDate = date
                }
            }
            btnEndDate.setOnClickListener {
                showDatePicker { date ->
                    binding.tvEndDate.text =
                        SimpleDateFormat("dd - MMMM - yyyy", Locale.getDefault()).format(date)
                    endDate = date
                }
            }

            btnSave.setOnClickListener {
                showConfirmationAddProject()
            }

        }
    }

    private fun addProject() {
        if (validateInputs()) {
            lifecycleScope.launch {
                try {
                    CommonHelper.showLoading(
                        this@AdminAddProjectActivity,
                        binding.loadingBar,
                        binding.loadingOverlay
                    )
                    val imageUrl = photoUri?.let {
                        cloudinaryHelper.uploadProjectImage(it, "Home", "projects")
                    } ?: throw Exception("No Image Selected")

                    viewModel.addProject(
                        binding.edtNameProject.text.toString(),
                        binding.edtLocationProject.text.toString(),
                        startDate ?: Date(),
                        endDate ?: Date(),
                        binding.edtStatusProject.text.toString(),
                        binding.edtDescriptionProject.text.toString(),
                        imageUrl
                    )
                    CommonHelper.hideLoading(binding.loadingBar, binding.loadingOverlay)
                    finish()
                } catch (e: Exception) {
                    CommonHelper.hideLoading(binding.loadingBar, binding.loadingOverlay)
                    Log.e("AdminAddProjectActivity", "Error : ${e.message}")
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validasi Project Name
        if (binding.edtNameProject.text.isNullOrEmpty()) {
            binding.edtNameProject.error = getString(R.string.error_empty_project_name)
            isValid = false
        } else {
            binding.edtNameProject.error = null
        }

        // Validasi Location
        if (binding.edtLocationProject.text.isNullOrEmpty()) {
            binding.edtLocationProject.error = getString(R.string.error_empty_location)
            isValid = false
        } else {
            binding.edtLocationProject.error = null
        }

        // Validasi Status Project
        if (binding.edtStatusProject.text.isNullOrEmpty()) {
            binding.edtStatusProjectLayout.error = getString(R.string.error_empty_status)
            isValid = false
        } else {
            binding.edtStatusProjectLayout.error = null
        }

        return isValid
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePickerDialog = DatePickerDialog(
            this, { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }
                onDateSelected(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun openCamera() {
        val photoFile = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        takePictureLauncher.launch(photoUri!!)
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
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
                ), ClockOutActivity.REQUEST_PERMISSIONS
            )
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
                ), ClockOutActivity.REQUEST_PERMISSIONS
            )
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsModern()
        } else {
            requestPermissionsLegacy()
        }
    }

    private fun setupCameraAndGalleryLaunchers() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && photoUri != null) {
                    Glide.with(this)
                        .load(photoUri)
                        .into(binding.ivImage)
                    //validateClockOutButton()
                } else {
                    Toast.makeText(this, "Failed to take picture", Toast.LENGTH_SHORT).show()
                    //validateClockOutButton()
                }
            }

        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    photoUri = uri
                    Glide.with(this)
                        .load(uri)
                        .into(binding.ivImage)
                    //validateClockOutButton()
                } else {
                    Toast.makeText(this, "Failed to pick image", Toast.LENGTH_SHORT).show()
                    //validateClockOutButton()
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

