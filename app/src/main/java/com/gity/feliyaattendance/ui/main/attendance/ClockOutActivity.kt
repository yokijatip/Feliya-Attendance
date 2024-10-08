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
import com.gity.feliyaattendance.data.local.ProjectDataStoreManager
import com.gity.feliyaattendance.databinding.ActivityClockOutBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

@Suppress("DEPRECATION")
class ClockOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClockOutBinding
    private var photoUri: Uri? = null
    private var clockOutTime: Timestamp? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var repository: Repository
    private lateinit var viewModel: AttendanceViewModel

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
        getClockInData()
        displayProjectData()

        binding.apply {
            btnBack.setOnClickListener { onBackPressed() }
            btnClockOut.setOnClickListener { clockOut() }
            openGalleryOrCamera.setOnClickListener { checkAndRequestPermissions() }

            tvTitle.setOnClickListener {
                val value = hashMapOf(
                    "user_id" to "123userid",
                    "project_id" to "123projectid",
                    "date" to Timestamp.now(),
                    "clock_in_time" to Timestamp.now(),
                    "clock_out_time" to Timestamp.now(), // Nullable
                    "work_proof_in" to "imagein",
                    "work_proof_out" to "imageout", // Nullable
                    "work_description" to "123desc",
                    "status" to "pending",
                    "work_hours" to 8,
                    "overtime_hours" to 2
                )

                firebaseFirestore.collection("attendance")
                    .add(value)
            }
        }

    }

    private fun getClockInData() {
        lifecycleScope.launch {

        }
    }


    private fun clockOut() {
        lifecycleScope.launch {


//            Log.v(
//                "ATTENDANCE_DATA",
//                "User Id: $userId | Project Id: $projectId | Date: $date | Clock In Time: $clockInTime | Clock Out Time: $clockOutTime | Image URL In: $imageUrlIn | Image URL Out: $imageUrlOut | Description: $description"
//            )
        }
    }

    private fun calculateWorkHours(clockIn: Timestamp, clockOut: Timestamp): Int {
        val duration =
            clockOut.toDate().time - clockIn.toDate().time // Menghitung selisih waktu dalam milidetik
        return (duration / (1000 * 60 * 60)).toInt() // Mengonversi milidetik menjadi jam
    }

    private fun calculateOvertime(workHours: Int): Int {
        // Contoh perhitungan overtime, anggap 8 jam kerja reguler
        return if (workHours > 8) workHours - 8 else 0
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
                } else {
                    Toast.makeText(this, "Failed to take picture", Toast.LENGTH_SHORT).show()
                }
            }

        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    Toast.makeText(this, "Image selected: $uri", Toast.LENGTH_SHORT).show()
                    binding.tvImageUrl.text = uri.toString()
                } else {
                    Toast.makeText(this, "Failed to pick image", Toast.LENGTH_SHORT).show()
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
}
