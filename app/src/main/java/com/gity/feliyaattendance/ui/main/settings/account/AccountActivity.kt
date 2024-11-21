package com.gity.feliyaattendance.ui.main.settings.account

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAccountBinding
import com.gity.feliyaattendance.helper.CloudinaryHelper
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private val viewModel: AccountViewModel by viewModels {
        ViewModelFactory(Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()))
    }
    private val cloudinaryHelper by lazy { CloudinaryHelper(this) }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            handleImageResult(success) { photoUri }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleImageResult(uri != null) { uri }
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            showImageSelectionDialog()
        } else {
            Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show()
        }
    }

    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        setupWindowInsets()
        setupUserDetails()
        setupEventListeners()
        setupBackNavigation()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupUserDetails() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        viewModel.loadUserDetails(userId)
        viewModel.userDetails.observe(this) { result ->
            result.onSuccess { user ->
                binding.apply {
                    tvUserName.text = user.name
                    tvUserEmail.text = user.email
                    user.profileImageUrl?.let { imageUrl ->
                        Glide.with(this@AccountActivity).load(imageUrl)
                            .placeholder(R.drawable.iv_placeholder).error(R.drawable.iv_placeholder)
                            .into(ivUserProfile)
                    }
                }
            }
            result.onFailure { e ->
                CommonHelper.showInformationFailedDialog(
                    this, getString(R.string.error), e.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    private fun setupEventListeners() {
        binding.apply {
            ivUserProfile.setOnClickListener { checkAndRequestPermissions() }
            btnUploadImageProfile.setOnClickListener { checkAndRequestPermissions() }
        }

        viewModel.profileImageUploadResult.observe(this) { result ->
            result.onSuccess { imageUrl ->
                Glide.with(this).load(imageUrl).into(binding.ivUserProfile)
                CommonHelper.showInformationSuccessDialog(
                    this, getString(R.string.successfully), getString(R.string.success_upload_image)
                )
            }.onFailure {
                CommonHelper.showInformationFailedDialog(
                    this, getString(R.string.error), getString(R.string.failed_upload_image)
                )
            }
        }
    }

    private fun setupBackNavigation() {
        binding.btnBack.setOnClickListener { finish() }
        onBackPressedDispatcher.addCallback(this) { finish() }
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = getRequiredPermissions()
        val deniedPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isEmpty()) {
            showImageSelectionDialog()
        } else {
            permissionLauncher.launch(deniedPermissions.toTypedArray())
        }
    }

    private fun getRequiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        }
    }

    private fun showImageSelectionDialog() {
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.custom_dialog_image_and_camera)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            findViewById<LinearLayout>(R.id.linear_layout_camera).setOnClickListener {
                openCamera()
                dismiss()
            }
            findViewById<LinearLayout>(R.id.linear_layout_gallery).setOnClickListener {
                openGallery()
                dismiss()
            }
            show()
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

    private fun handleImageResult(success: Boolean, uriProvider: () -> Uri?) {
        if (success) {
            photoUri = uriProvider()
            photoUri?.let { uri ->
                Glide.with(this).load(uri).into(binding.ivUserProfile)
                lifecycleScope.launch {
                    cloudinaryHelper.uploadUserProfileImage(uri).let { imageUrl ->
                        viewModel.uploadProfileImage(imageUrl)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
        }
    }
}