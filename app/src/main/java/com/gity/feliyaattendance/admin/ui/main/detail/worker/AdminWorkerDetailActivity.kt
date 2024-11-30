package com.gity.feliyaattendance.admin.ui.main.detail.worker

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.widget.LinearLayout
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
import com.gity.feliyaattendance.admin.ui.main.history.attendance.approved.HistoryAttendanceApprovedActivity
import com.gity.feliyaattendance.admin.ui.main.history.attendance.pending.HistoryAttendancePendingActivity
import com.gity.feliyaattendance.admin.ui.main.history.attendance.rejected.HistoryAttendanceRejectedActivity
import com.gity.feliyaattendance.data.model.DetailWorkerMenu
import com.gity.feliyaattendance.databinding.ActivityAdminWorkerDetailBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.StoragePermissionHandler
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
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
        observerUpdateStatus()
        handleMoreMenu()

    }

    private fun handleMoreMenu() {
        binding.apply {
            moreMenu.setOnClickListener {
                showDialogMoreMenu()
            }
        }
    }

    private fun activateAccount(): String {
        val activate = "activated"
        return activate
    }

    private fun suspendAccount(): String {
        val suspend = "suspended"
        return suspend
    }

    private fun showDialogMoreMenu() {
        val dialog = Dialog(this@AdminWorkerDetailActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.custom_dialog_menu_admin_worker_detail)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_activate_account).setOnClickListener {
            //viewModel.updateWorkerStatus(workerId, activateAccount())
            viewModel.updateStatusAccount(workerId, activateAccount())
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_suspend_account).setOnClickListener {
            viewModel.updateStatusAccount(workerId, suspendAccount())
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_delete_account).setOnClickListener {
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
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun observerUpdateStatus() {
        viewModel.updateStatusAccount.observe(this) { result ->
            result.onSuccess {
                CommonHelper.showInformationSuccessDialog(
                    this@AdminWorkerDetailActivity,
                    getString(R.string.successfully),
                    getString(R.string.process_success)
                )
            }.onFailure {
                CommonHelper.showInformationFailedDialog(
                    this@AdminWorkerDetailActivity,
                    getString(R.string.failed),
                    getString(R.string.process_failed)
                )
            }

        }
    }

    private fun workerDetailMenuSetup() {
        val workerDetailMenuList = listOf(
            DetailWorkerMenu(getString(R.string.admin_menu_generate_excel), R.drawable.ic_table),
            DetailWorkerMenu(
                getString(R.string.history_approved),
                R.drawable.ic_check_square_custom_admin
            ),
            DetailWorkerMenu(getString(R.string.history_rejected), R.drawable.ic_minus_circle),
            DetailWorkerMenu(getString(R.string.history_pending), R.drawable.ic_clock)
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

                getString(R.string.history_approved) -> {
                    startActivity(
                        Intent(
                            this@AdminWorkerDetailActivity,
                            HistoryAttendanceApprovedActivity::class.java
                        )
                    )
                }

                getString(R.string.history_rejected) -> {
                    startActivity(
                        Intent(
                            this@AdminWorkerDetailActivity,
                            HistoryAttendanceRejectedActivity::class.java
                        )
                    )
                }

                getString(R.string.history_pending) -> {
                    startActivity(
                        Intent(
                            this@AdminWorkerDetailActivity,
                            HistoryAttendancePendingActivity::class.java
                        )
                    )
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
            result.onSuccess {
                // Langsung membuka file yang berhasil dibuat
                showSuccessSnackbar()
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

            // Coba membuka langsung tanpa memilih aplikasi
            startActivity(intent)
        } catch (e: Exception) {
            // Jika gagal membuka langsung, gunakan chooser
            try {
                val chooserIntent = Intent.createChooser(intent, "Buka dengan")
                startActivity(chooserIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Tidak dapat membuka file: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showSuccessSnackbar() {
        val snackbar = Snackbar.make(
            binding.root,
            "Excel berhasil dihasilkan! Akses di Documents > LaporanAbsensi",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Buka") {
            // Arahkan pengguna ke folder Documents jika diperlukan
            openDocumentsFolder()
        }
        snackbar.show()
    }

    private fun openDocumentsFolder() {
        val documentsFolder = File("/storage/emulated/0/Documents/LaporanAbsensi")
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            documentsFolder
        )
        intent.setDataAndType(uri, "resource/folder")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    // Fungsi ini memeriksa hasil permintaan izin
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == StoragePermissionHandler.STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    generateExcel(workerId)
                } else {
                    Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    generateExcel(workerId)
                } else {
                    Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}