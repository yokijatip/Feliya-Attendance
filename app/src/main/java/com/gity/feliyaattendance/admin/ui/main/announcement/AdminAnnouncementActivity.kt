package com.gity.feliyaattendance.admin.ui.main.announcement

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.adapter.AdminAnnouncementAdapter
import com.gity.feliyaattendance.admin.ui.main.detail.announcement.AdminAnnouncementDetailActivity
import com.gity.feliyaattendance.databinding.ActivityAdminAnnouncementBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class AdminAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAnnouncementBinding
    private lateinit var viewModel: AdminAnnouncementViewModel

    private lateinit var adapter: AdminAnnouncementAdapter
    private var selectedDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminAnnouncementBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI()
        setupViewModel()
        navigateToAddPost()
        setupRecyclerView()
        observeAnnouncements()
        setupFilterButton()
        setupSwipeRefreshLayout()
        viewModel.fetchAnnouncements()
    }

    private fun setupSwipeRefreshLayout() {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.fetchAnnouncements()
            }
        }
    }

    //    Setup RecyclerView
    private fun setupRecyclerView() {
        adapter = AdminAnnouncementAdapter { announcement ->
            // Handle announcement click if needed
            val intent = Intent(this@AdminAnnouncementActivity, AdminAnnouncementDetailActivity::class.java)
            intent.putExtra("announcementId", announcement.id)
            startActivity(intent)
        }
        binding.rvAnnouncement.adapter = adapter
        binding.rvAnnouncement.layoutManager = LinearLayoutManager(this)

        val dividerItemDecoration =
            DividerItemDecoration(binding.rvAnnouncement.context, LinearLayoutManager.VERTICAL)
        binding.rvAnnouncement.addItemDecoration(dividerItemDecoration)
    }

    //    Setup Observer Announcement
    private fun observeAnnouncements() {
        viewModel.announcementList.observe(this) { result ->
            result.onSuccess {
                adapter.submitList(result.getOrNull())
                binding.tvNoAnnouncement.visibility =
                    if (result.getOrNull()?.isEmpty() == true) View.VISIBLE
                    else View.GONE
                // Stop refreshing animation
                binding.swipeRefreshLayout.isRefreshing = false
            }.onFailure {
                CommonHelper.showInformationFailedDialog(
                    this@AdminAnnouncementActivity,
                    getString(R.string.failed),
                    getString(R.string.no_announcement_description)
                )
                // Stop refreshing animation
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
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

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Month")
                .build()

            datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
                selectedDate = Date(selectedDateInMillis)
                viewModel.fetchAnnouncements(selectedDate)
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }
    }
}