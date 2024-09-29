package com.gity.feliyaattendance.utils

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class OfflinePersistence: Application () {

    override fun onCreate() {
        super.onCreate()

        // Aktifkan Firebase Firestore Offline Persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)  // Mengaktifkan offline persistence
            .build()

        // Terapkan pengaturan ke instance Firebase Firestore
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}