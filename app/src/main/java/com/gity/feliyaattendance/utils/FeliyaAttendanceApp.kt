package com.gity.feliyaattendance.utils

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FeliyaAttendanceApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initCloudinary()
        firebaseFirestore()

    }

    private fun initCloudinary() {
        val config = mapOf(
            "cloud_name" to "dhmxpasaz",
            "api_key" to "959928627833379",
            "api_secret" to "JIj6eDiJEugY9cDVZzbt1Om7QcY"
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