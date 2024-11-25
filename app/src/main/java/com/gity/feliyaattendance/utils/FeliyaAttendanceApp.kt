package com.gity.feliyaattendance.utils

import android.app.Application
import com.cloudinary.android.MediaManager
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

        val dotEnv = dotenv {
            directory = "."
            filename = ".env"
        }


        val config = mapOf(
            "cloud_name" to dotEnv["CLOUDINARY_NAME"],
            "api_key" to dotEnv["CLOUDINARY_MY_API_KEY"],
            "api_secret" to dotEnv["CLOUDINARY_MY_API_SECRET"]
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