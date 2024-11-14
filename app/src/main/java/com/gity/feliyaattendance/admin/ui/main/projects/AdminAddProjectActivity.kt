package com.gity.feliyaattendance.admin.ui.main.projects

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        cloudinaryHelper = CloudinaryHelper(this@AdminAddProjectActivity)

        initializeComponents()
        setupUI()
        setupValidations()
        setupObservers()
    }

    private fun setupValidations() {
        // Add text change listeners to all edit texts
        binding.apply {
            edtNameProject.addTextChangedListener(createTextWatcher())
            edtLocationProject.addTextChangedListener(createTextWatcher())
            edtStatusProject.addTextChangedListener(createTextWatcher())
            edtDescriptionProject.addTextChangedListener(createTextWatcher())
        }
    }

    private fun initializeComponents() {
        // Initialize ViewModel
        repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminProjectViewModel::class.java]

        // Setup other components
        setupStatusProjectDropdown()
        setupListeners()
        setupCameraAndGalleryLaunchers()
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
        }
    }


    private fun validateForm() {
        val isValid = isFormValid()
        binding.btnSave.isEnabled = isValid

        // Optional: Change button appearance based on validity
        binding.btnSave.alpha = if (isValid) 1.0f else 0.5f
    }

    private fun isFormValid(): Boolean {
        return binding.run {
            !edtNameProject.text.isNullOrEmpty() &&
                    !edtLocationProject.text.isNullOrEmpty() &&
                    !edtStatusProject.text.isNullOrEmpty() &&
                    !edtDescriptionProject.text.isNullOrEmpty() &&
                    photoUri != null && // Check if image is selected
                    startDate != null && // Check if start date is selected
                    endDate != null && // Check if end date is selected
                    validateDates() // Check if dates are valid
        }
    }

    private fun validateDates(): Boolean {
        return if (startDate != null && endDate != null) {
            startDate!!.before(endDate) || startDate!! == endDate
        } else {
            false
        }
    }

    private fun setupStatusProjectDropdown() {
        statusProjectAdapter = ArrayAdapter(this, R.layout.list_item_roles, setupStatusProject())
        binding.edtStatusProject.setAdapter(statusProjectAdapter)
    }

    private fun setupListeners() {
        binding.apply {
            btnStartDate.setOnClickListener {
                showDatePicker { date ->
                    startDate = date
                    tvStartDate.text = date.formatToString()
                    validateForm()
                }
            }

            btnEndDate.setOnClickListener {
                showDatePicker { date ->
                    endDate = date
                    tvEndDate.text = date.formatToString()
                    validateForm()
                }
            }

            btnSave.apply {
                isEnabled = false
                alpha = 0.5f
                setOnClickListener {
                    showConfirmationAddProject()
                }
            }

            openGalleryOrCamera.setOnClickListener { checkAndRequestPermissions() }
            linearLayoutOpenGalleryOrCamera.setOnClickListener { checkAndRequestPermissions() }
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

    private fun setupObservers() {
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

    private fun setupStatusProject(): List<String> {
        return listOf(
            getString(R.string.status_project_active),
            getString(R.string.status_project_inactive),
            getString(R.string.status_project_completed)
        )
    }

    private fun setupCameraAndGalleryLaunchers() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && photoUri != null) {
                    updateProjectImage(photoUri!!)
                } else {
                    showError(getString(R.string.error_taking_picture))
                }
            }

        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    photoUri = it
                    updateProjectImage(it)
                } ?: showError(getString(R.string.error_picking_image))
            }
    }

    private fun addProject() {
        if (!isFormValid()) return

        lifecycleScope.launch {
            try {
                showLoading(true)

                val imageUrl = photoUri?.let {
                    cloudinaryHelper.uploadProjectImage(it, "Home", "projects")
                } ?: throw Exception("No Image Selected")

                viewModel.addProject(
                    projectName = binding.edtNameProject.text.toString(),
                    location = binding.edtLocationProject.text.toString(),
                    startDate = startDate!!,
                    endDate = endDate!!,
                    status = binding.edtStatusProject.text.toString(),
                    description = binding.edtDescriptionProject.text.toString(),
                    projectImage = imageUrl
                )

                showLoading(false)
                finish()
            } catch (e: Exception) {
                showLoading(false)
                showError(e.message ?: getString(R.string.error_general))
                Log.e("AdminAddProjectActivity", "Error: ${e.message}")
            }
        }
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

    private fun updateProjectImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(binding.ivImage)
        validateForm()
    }

    private fun handleBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        onBackPressedDispatcher.addCallback {
            finish()
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            CommonHelper.showLoading(
                this@AdminAddProjectActivity,
                binding.loadingBar,
                binding.loadingOverlay
            )
        } else {
            CommonHelper.hideLoading(binding.loadingBar, binding.loadingOverlay)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun Date.formatToString(): String {
        return SimpleDateFormat("dd - MMMM - yyyy", Locale.getDefault()).format(this)
    }
}

