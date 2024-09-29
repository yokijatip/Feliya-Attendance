package com.gity.feliyaattendance.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.ui.main.MainAdminActivity
import com.gity.feliyaattendance.databinding.ActivitySplashScreenBinding
import com.gity.feliyaattendance.ui.auth.AuthActivity
import com.gity.feliyaattendance.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            checkUserLogin()
        }

    }

    private suspend fun checkUserLogin() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            try {
//                Ambil data user dari firestore
                val snapshot =
                    firebaseFirestore.collection("users").document(currentUser.uid).get().await()
                val role = snapshot.getString("role")

//                Arahkan user ke halaman sesuai role
                when (role) {
                    "worker" -> {
                        navigateToMain()
                    }
                    "admin" -> {
                        navigateToAdmin()
                    }
                    else -> {
                        navigateToAuth() // Jika role tidak ditemukan
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                navigateToAuth()
            }
        } else {
            navigateToAuth()
        }
    }

    private fun navigateToAdmin() {
        val intent = Intent(this@SplashScreenActivity, MainAdminActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToAuth() {
        val intent = Intent(this@SplashScreenActivity, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}