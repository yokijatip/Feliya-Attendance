package com.gity.feliyaattendance.admin.ui.main.detail.project

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAdminProjectDetailBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminProjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminProjectDetailBinding
    private lateinit var viewModel: AdminProjectDetailViewModel
    private lateinit var statusProjectAdapter: ArrayAdapter<String>

    private var projectId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminProjectDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        projectId = intent.getStringExtra("PROJECT_ID")
        setupUI()
        setupViewModel()
        projectId?.let { viewModel.fetchProjectDetail(it) }
        observerProjectDetail()
        setupStatusProjectDropdown()
    }

    private fun observerProjectDetail() {
        viewModel.projectDetail.observe(this@AdminProjectDetailActivity) { detail ->
            if (detail != null) {
                binding.apply {
                    tvProjectName.text = detail.projectName
                    tvProjectLocation.text = detail.location
                    tvEndDate.text = CommonHelper.formatTimestamp(detail.endDate)
                    tvStartDate.text = CommonHelper.formatTimestamp(detail.startDate)
                    tvProjectDescription.text = detail.description
                    Glide.with(this@AdminProjectDetailActivity)
                        .load(detail.projectImage)
                        .into(ivDetailProject)
                    updateStatusUI(detail.status)
                }
            } else {
                Log.e("AdminProjectDetailActivity", "Project detail not found")
            }
        }
    }

    private fun setupSwipeRefreshLayout() {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                projectId?.let { viewModel.fetchProjectDetail(it) }
                viewModel.projectDetail.observe(this@AdminProjectDetailActivity) { detail ->
                    if (projectId != null) {
                        updateStatusUI(detail?.status)
                        swipeRefreshLayout.isRefreshing = false
                    } else {
                        Log.e("AdminProjectDetailActivity", "Project detail not found")
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun updateStatusUI(status: String? = null) {
        val (backgroundColor, textColor) = when (status) {
            "Completed" -> Pair(
                R.color.status_pending_background,
                R.color.status_pending
            )

            "Inactive" -> Pair(
                R.color.status_rejected_background,
                R.color.status_rejected
            )

            "Active" -> Pair(
                R.color.status_approved_background,
                R.color.status_approved
            )

            else -> Pair(
                R.color.status_pending_background,
                R.color.status_pending
            )
        }

        binding.apply {
            // Update CardView background
            cardStatus.setBackgroundTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(this@AdminProjectDetailActivity, backgroundColor)
                )
            )

            // Update TextView color and text
            tvStatus.apply {
                setTextColor(
                    ContextCompat.getColor(
                        this@AdminProjectDetailActivity,
                        textColor
                    )
                )
                text = status
            }
        }
    }

    private fun setupUI() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        handleBackButton()
        setupSwipeRefreshLayout()
    }

    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminProjectDetailViewModel::class.java]
    }

    private fun handleBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        onBackPressedDispatcher.addCallback {
            finish()
        }
    }

    private fun setupStatusProjectDropdown() {
        statusProjectAdapter = ArrayAdapter(this, R.layout.list_item_roles, setupStatusProject())
        binding.edtStatusProject.setAdapter(statusProjectAdapter)
    }

    private fun setupStatusProject(): List<String> {
        return listOf(
            "Active",
            "Inactive",
            "Completed"
        )
    }


}