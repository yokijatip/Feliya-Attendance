package com.gity.feliyaattendance.utils

import android.app.Application
import com.cloudinary.android.MediaManager
import com.gity.feliyaattendance.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import io.github.cdimascio.dotenv.dotenv

@Suppress("DEPRECATION")
class FeliyaAttendanceApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initCloudinary()
        firebaseFirestore()

    }

    private fun initCloudinary() {

        val config = mapOf(
            "cloud_name" to resources.getString(R.string.CLOUDINARY_NAME),
            "api_key" to resources.getString(R.string.MY_API_KEY),
            "api_secret" to resources.getString(R.string.MY_API_SECRET)
        )
        MediaManager.init(this, config)
    }

    private fun firebaseFirestore() {
        // Aktifkan Firebase Firestore Offline Persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)  // Mengaktifkan offline persistence
            .build()

        // Terapkan pengaturan ke instance Firebase Firestore
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}