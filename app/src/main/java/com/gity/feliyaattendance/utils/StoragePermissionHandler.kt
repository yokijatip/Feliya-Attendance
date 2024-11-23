package com.gity.feliyaattendance.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class StoragePermissionHandler(private val context: Context) {

    companion object {
        const val STORAGE_PERMISSION_CODE = 100
    }

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Memeriksa apakah aplikasi memiliki akses ke semua file
            Environment.isExternalStorageManager()
        } else {
            // Memeriksa izin WRITE_EXTERNAL_STORAGE untuk versi lebih rendah dari Android 11
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Jika di Android 11 atau lebih tinggi, minta akses ke semua file
            if (!hasStoragePermission()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${context.packageName}")
                activity.startActivityForResult(intent, STORAGE_PERMISSION_CODE)
            }
        } else {
            // Untuk versi lebih rendah, minta izin WRITE_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }
}