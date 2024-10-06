package com.gity.feliyaattendance.ui.main.attendance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.adapter.ProjectAdapter
import com.gity.feliyaattendance.databinding.ActivityShowProjectBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShowProjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowProjectBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var repository: Repository
    private lateinit var viewModel: AttendanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityShowProjectBinding.inflate(layoutInflater)
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

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AttendanceViewModel::class.java]

        val adapter = ProjectAdapter { selectedProject ->
            val intent = Intent(this, ClockInActivity::class.java).apply {
                putExtra("SELECTED_PROJECT", selectedProject)
                putExtra("PROJECT_ID", selectedProject.projectId)
                putExtra("PROJECT_NAME", selectedProject.projectName)
                putExtra("PROJECT_LOCATION", selectedProject.location)
            }
            startActivity(intent)
        }


        binding.rvProjects.layoutManager = LinearLayoutManager(this@ShowProjectActivity)
        binding.rvProjects.adapter = adapter


        viewModel.activeProjects.observe(this) { result ->
            result.onSuccess { projects ->
                adapter.submitList(projects)
            }.onFailure { exception ->
                Log.e("ShowProjectActivity", "Error fetching active projects", exception)
                // Tampilkan pesan kesalahan ke pengguna jika diperlukan
            }
        }


        viewModel.fetchActiveProjects()
    }
}