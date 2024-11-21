package com.gity.feliyaattendance.admin.ui.main.announcement

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAdminAnnouncementBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAnnouncementBinding
    private lateinit var viewModel: AdminAnnouncementViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminAnnouncementBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI()
        setupViewModel()
        navigateToAddPost()
    }

    //    Setup ViewModel
    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminAnnouncementViewModel::class.java]
    }

    //    SetupUI
    private fun setupUI() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        handleBackButton()
    }

    //    Handle Back Button
    private fun handleBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        onBackPressedDispatcher.addCallback {
            finish()
        }
    }

    //    Navigate to add post
    private fun navigateToAddPost() {
        binding.fabAddPost.setOnClickListener {
            Intent(this@AdminAnnouncementActivity, AdminAddPostActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}