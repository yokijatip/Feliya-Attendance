package com.gity.feliyaattendance.admin.ui.main.home.workers

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
import com.gity.feliyaattendance.admin.adapter.AdminWorkerAdapter
import com.gity.feliyaattendance.admin.ui.main.detail.worker.AdminWorkerDetailActivity
import com.gity.feliyaattendance.databinding.ActivityAdminListWorkerBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminListWorkerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminListWorkerBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var viewModel: AdminWorkersViewModel
    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminListWorkerBinding.inflate(layoutInflater)
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
        viewModel = ViewModelProvider(this, factory)[AdminWorkersViewModel::class.java]

        val adapter = AdminWorkerAdapter { worker ->
            worker.id?.let { navigateToWorkerDetail(it) }
        }

        binding.rvWorkerList.layoutManager = LinearLayoutManager(this)
        binding.rvWorkerList.adapter = adapter

        viewModel.workerlist.observe(this) { result ->
            result.onSuccess {
                adapter.submitList(it)
            }.onFailure {
                Log.e("WORKERS_DATA", "Error fetching Worker", it)
            }
        }

        viewModel.getWorkerList()
        swipeRefreshLayout(adapter)

    }

    private fun swipeRefreshLayout(adapter: AdminWorkerAdapter) {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.getWorkerList()
                viewModel.workerlist.observe(this@AdminListWorkerActivity) { result ->
                    result.onSuccess {
                        adapter.submitList(it)
                    }.onFailure {
                        Log.e("WORKERS_DATA", "Error fetching Worker", it)
                    }
                }
            }
        }
    }

    private fun navigateToWorkerDetail(workerId: String) {
        val intent = Intent(this, AdminWorkerDetailActivity::class.java)
        intent.putExtra("WORKER_ID", workerId)
        startActivity(intent)
    }
}