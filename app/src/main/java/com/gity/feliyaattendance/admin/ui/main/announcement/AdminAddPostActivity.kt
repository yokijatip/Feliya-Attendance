package com.gity.feliyaattendance.admin.ui.main.announcement

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.ui.main.home.AdminHomeViewModel
import com.gity.feliyaattendance.databinding.ActivityAdminAddPostBinding
import com.gity.feliyaattendance.helper.CloudinaryHelper
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.main.attendance.ClockInActivity
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File

class AdminAddPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAddPostBinding
    private lateinit var viewModel: AdminAnnouncementViewModel
    private lateinit var viewModelHomeFragment: AdminHomeViewModel

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private lateinit var cloudinaryHelper: CloudinaryHelper
    private lateinit var usernamePost: String
    private lateinit var emailPost: String
    private lateinit var imageProfileUrl: String

    private var photoUri: Uri? = null

    companion object {
        const val REQUEST_PERMISSIONS = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminAddPostBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI()
        initializeComponents()
        setupCloudinary()
        setupListener()
        setupValidations()
        observerEmailAndUsername()
        setupObserver()
    }

    private fun showConfirmationAddProject() {
        CommonHelper.showConfirmationDialog(
            context = this@AdminAddPostActivity,
            title = getString(R.string.title_dialog_confirmation),
            description = getString(R.string.description_dialog_confirmation_default),
            positiveButtonText = getString(R.string.sure),
            negativeButtonText = getString(R.string.cancel),
            onPositiveClick = {
                addAnnouncement()
            }
        )
    }

    private fun setupValidations() {
        binding.apply {
            edtAnnouncement.addTextChangedListener(createTextWatcher())
        }
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

    private fun isFormValid(): Boolean {
        return binding.run {
            !edtAnnouncement.text.isNullOrEmpty()
        }
    }

    private fun validateForm() {
        val isValid = isFormValid()
        binding.btnSave.isEnabled = isValid

        binding.btnSave.alpha = if (isValid) 1.0f else 0.5f
    }

    private fun addAnnouncement() {
        if (!isFormValid()) return

        lifecycleScope.launch {
            try {
                showLoading(true)

                // Ubah logika upload image menjadi opsional
                val imageUrl = photoUri?.let {
                    cloudinaryHelper.uploadAnnouncementImage(it, "Home", "announcement")
                } ?: "" // Jika tidak ada foto, set imageUrl sebagai string kosong

                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid

                viewModel.addPost(
                    announcement = binding.edtAnnouncement.text.toString(),
                    createdBy = userId!!,
                    createdByName = usernamePost,
                    createdByEmail = emailPost,
                    imageAnnouncement = imageUrl,
                    imageProfile = imageProfileUrl
                )

                showLoading(false)
                finish()
            } catch (e: Exception) {
                showLoading(false)
                Log.e("AdminAddProjectActivity", "Error: ${e.message}")
                CommonHelper.showInformationFailedDialog(
                    this@AdminAddPostActivity,
                    getString(R.string.failed),
                    "Error: ${e.message}"
                )
            }
        }
    }

    private fun observerEmailAndUsername() {
        viewModelHomeFragment.emailResult.observe(this@AdminAddPostActivity) { result ->
            result.onSuccess { email ->
                emailPost = email
            }.onFailure { exception ->
                Log.e("Announcement", "Error: ${exception.message}")
            }
        }

        viewModelHomeFragment.nameResult.observe(this@AdminAddPostActivity) { result ->
            result.onSuccess { name ->
                usernamePost = name
            }.onFailure { exception ->
                Log.e("Announcement", "Error: ${exception.message}")
            }
        }

        viewModelHomeFragment.imageProfileUrl.observe(this@AdminAddPostActivity) { result ->
            result.onSuccess { imageUrl ->
                imageProfileUrl = imageUrl
                Log.d("GetImageUrlProfile", "Result : $imageUrl")
            }.onFailure {
                Log.e("Announcement", "Error : ${it.message}")
            }
        }
    }

    private fun setupCameraAndGalleryLaunchers() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && photoUri != null) {
                    updateProjectImage(photoUri!!)
                } else {
                    CommonHelper.showInformationFailedDialog(
                        this@AdminAddPostActivity,
                        getString(R.string.failed),
                        "Failed to get Image"
                    )
                }
            }

        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    photoUri = it
                    updateProjectImage(it)
                } ?: CommonHelper.showInformationFailedDialog(
                    this@AdminAddPostActivity,
                    getString(R.string.failed),
                    "Failed to get Image"
                )
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

    private fun setupListener() {
        binding.apply {
            openGalleryOrCamera.setOnClickListener {
                checkAndRequestPermissions()
            }
            btnSave.apply {
                isEnabled = false
                alpha = 0.5f
                setOnClickListener {
                    showConfirmationAddProject()
                }
            }
        }
    }

    private fun setupObserver() {
        viewModel.createAnnouncement.observe(this) { result ->
            result.onSuccess {
                CommonHelper.showInformationSuccessDialog(
                    this@AdminAddPostActivity,
                    getString(R.string.successfully),
                    getString(R.string.success_create_announcement)
                )
                finish()
            }.onFailure { exception ->
                CommonHelper.showInformationFailedDialog(
                    this@AdminAddPostActivity,
                    getString(R.string.failed),
                    "Error: $exception, ${getString(R.string.process_failed)}"
                )
            }

        }
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

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA
                )

            else ->
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
        }

        if (permissionsToRequest.all {
                checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }) {
            showDialogImage()
        } else {
            requestPermissions(permissionsToRequest, ClockInActivity.REQUEST_PERMISSIONS)
        }
    }

    //    Setup ViewModel
    private fun initializeComponents() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminAnnouncementViewModel::class.java]
        viewModelHomeFragment = ViewModelProvider(this, factory)[AdminHomeViewModel::class.java]

        setupCameraAndGalleryLaunchers()
    }

    //    Setup Cloudinary
    private fun setupCloudinary() {
        cloudinaryHelper = CloudinaryHelper(this@AdminAddPostActivity)
    }

    private fun updateProjectImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(binding.ivImage)
    }


    private fun showLoading(show: Boolean) {
        if (show) {
            CommonHelper.showLoading(
                this@AdminAddPostActivity,
                binding.loadingBar,
                binding.loadingOverlay
            )
        } else {
            CommonHelper.hideLoading(binding.loadingBar, binding.loadingOverlay)
        }
    }

    //    Handle Back Button
    private fun handleBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback {
            finish()
        }
    }
}