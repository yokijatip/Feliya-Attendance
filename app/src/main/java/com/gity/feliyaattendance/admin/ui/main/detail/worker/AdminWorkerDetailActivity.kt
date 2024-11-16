package com.gity.feliyaattendance.admin.ui.main.detail.worker

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.adapter.WorkerDetailAdapter
import com.gity.feliyaattendance.admin.data.model.Worker
import com.gity.feliyaattendance.data.model.DetailWorkerMenu
import com.gity.feliyaattendance.databinding.ActivityAdminWorkerDetailBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.StoragePermissionHandler
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.Date

class AdminWorkerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminWorkerDetailBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var viewModel: AdminWorkerDetailViewModel
    private lateinit var repository: Repository

    private lateinit var workerName: String
    private lateinit var workerId: String

    private lateinit var storagePermissionHandler: StoragePermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminWorkerDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storagePermissionHandler = StoragePermissionHandler(this)
        handleBackButton()

        // Pastikan izin sudah di-check di awal
        if (!storagePermissionHandler.hasStoragePermission()) {
            storagePermissionHandler.requestStoragePermission(this)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminWorkerDetailViewModel::class.java]

//        Get Worker ID
        workerId = intent.getStringExtra("WORKER_ID")!!

//        Observer ViewModel
        observerDetailViewModel()
        workerDetailMenuSetup()

        viewModel.fetchWorkerDetail(workerId)

        observeExcelGeneration()

        binding.apply {
            moreMenu.setOnClickListener {
                CommonHelper.showToast(this@AdminWorkerDetailActivity, "Under Development")
            }
        }

    }

    private fun workerDetailMenuSetup() {
        val workerDetailMenuList = listOf(
            DetailWorkerMenu(getString(R.string.admin_menu_generate_excel), R.drawable.ic_table),
            DetailWorkerMenu(getString(R.string.admin_menu_generate_pdf), R.drawable.ic_file_text),
            DetailWorkerMenu(
                getString(R.string.admin_menu_delete_account),
                R.drawable.ic_trash_solid
            )
        )

        val detailWorkerMenuAdapter = WorkerDetailAdapter(workerDetailMenuList) { menu ->
            when (menu.tvDetailWorkerMenu) {
                getString(R.string.admin_menu_generate_excel) -> {
                    if (storagePermissionHandler.hasStoragePermission()) {
                        // Generate Excel jika permission sudah diberikan
                        generateExcel(workerId)
                    } else {
                        storagePermissionHandler.requestStoragePermission(this)
                    }

                }

                getString(R.string.admin_menu_generate_pdf) -> {
                    Toast.makeText(
                        this@AdminWorkerDetailActivity,
                        "Generated PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                getString(R.string.admin_menu_delete_account) -> {
                    CommonHelper.showConfirmationDialog(
                        this@AdminWorkerDetailActivity,
                        title = getString(R.string.admin_menu_delete_account_title_confirmation),
                        description = getString(R.string.admin_menu_delete_account_description_confirmation),
                        positiveButtonText = "Yes",
                        negativeButtonText = "No",
                        onPositiveClick = {
                            viewModel.deleteWorkerAccount(workerId)
                            finish()
                        })
                }
            }
        }

        binding.rvWorkerDetailMenu.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailWorkerMenuAdapter
        }
    }

    private fun generateExcel(workerId: String) {
        val datePicker = MaterialDatePicker.Builder
            .dateRangePicker()
            .setTitleText("Pilih Rentang Tanggal")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val startTimestamp = Timestamp(Date(selection.first))
            val endTimestamp = Timestamp(Date(selection.second))

            viewModel.generateAttendanceReport(
                context = this,
                userId = workerId,
                username = workerName,
                startTimestamp = startTimestamp,
                endTimestamp = endTimestamp
            )
        }

        datePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun observeExcelGeneration() {
        viewModel.excelGenerationResult.observe(this) { result ->
            result.onSuccess { file ->
//                Buka file atau share
                openExcelFile(file)
            }.onFailure {
                Toast.makeText(
                    this@AdminWorkerDetailActivity,
                    "Error generating Excel file: $it",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observerDetailViewModel() {
        viewModel.workerDetailResult.observe(this) { result ->
            result.onSuccess { worker ->
                displayWorkerDetails(worker)
            }.onFailure { exception ->
                Toast.makeText(
                    this@AdminWorkerDetailActivity,
                    "Error fetching Worker Detail : $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayWorkerDetails(worker: Worker) {
        binding.apply {
            tvUserName.text = worker.name
            tvUserEmail.text = worker.email
            workerName = worker.name!!

            Glide.with(this@AdminWorkerDetailActivity)
                .load(worker.profileImageUrl)
                .apply(
                    RequestOptions.placeholderOf(R.drawable.worker_profile_placeholder)
                        .error(R.drawable.worker_profile_placeholder)
                )
                .into(ivUserProfile)
        }
    }

    private fun handleBackButton() {
        onBackPressedDispatcher.addCallback {
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    //    Fungsi untuk membuka file excel
    // TODO 1 : Handle Open File
    @SuppressLint("QueryPermissionsNeeded")
    private fun openExcelFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )


            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    uri,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Cek apakah ada aplikasi yang dapat menangani file
            val activities =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

            if (activities.isNotEmpty()) {
                // Gunakan createChooser untuk menampilkan dialog pemilihan aplikasi
                val chooserIntent = Intent.createChooser(intent, "Buka dengan aplikasi spreadsheet")
                startActivity(chooserIntent)
            } else {
                Toast.makeText(
                    this,
                    "Tidak ada aplikasi spreadsheet yang terinstall. Silakan install Microsoft Excel atau aplikasi spreadsheet lainnya.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // Fungsi ini memeriksa hasil permintaan izin
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == StoragePermissionHandler.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateExcel(workerId)
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

}