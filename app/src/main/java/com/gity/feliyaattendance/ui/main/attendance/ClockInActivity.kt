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
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityClockInBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ClockInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClockInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var repository: Repository
    private lateinit var viewModel: AttendanceViewModel
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var photoUri: Uri? = null
    private val calendar = Calendar.getInstance()

    companion object {
        const val REQUEST_PERMISSIONS = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClockInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        initFirebase()
        setupViewModel()
        setupCameraAndGalleryLaunchers()
        setupUI()

        binding.openGalleryOrCamera.setOnClickListener {
            checkAndRequestPermissions()
        }

        binding.btnStartCalendar.setOnClickListener {
            showDatePicker { date ->
                val formattedDate =
                    SimpleDateFormat("dd - MMMM - yyyy", Locale.getDefault()).format(date)
                binding.tvStartDate.text = formattedDate
            }
        }

        binding.btnOpenHour.setOnClickListener {
            showTimePicker { time ->
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(time)
                binding.tvStartClockIn.text = formattedTime
            }
        }
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
        // Inisialisasi launcher kamera
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && photoUri != null) {
                    Toast.makeText(this, "Picture taken: $photoUri", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to take picture", Toast.LENGTH_SHORT).show()
                }
            }

        // Inisialisasi launcher galeri
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    Toast.makeText(this, "Image selected: $uri", Toast.LENGTH_SHORT).show()
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
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA),
                REQUEST_PERMISSIONS
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
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                REQUEST_PERMISSIONS
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
        val dialog = Dialog(this@ClockInActivity).apply {
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
        // Mengambil URI dari FileProvider
        val photoFile = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        takePictureLauncher.launch(photoUri!!)
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Pilih Tanggal")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(Date(selection))
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker(onTimeSelected: (Date) -> Unit) {
        val timePicker = MaterialTimePicker.Builder().setTitleText("Pilih Waktu")
            .setTimeFormat(TimeFormat.CLOCK_24H).setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE)).build()

        timePicker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            onTimeSelected(calendar.time)
        }
        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }
}


