package com.gity.feliyaattendance.ui.main.explore

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.ui.main.detail.announcement.AdminAnnouncementDetailViewModel
import com.gity.feliyaattendance.databinding.ActivityAnnouncementDetailBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AnnouncementDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnouncementDetailBinding
    private lateinit var viewModel: AdminAnnouncementDetailViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAnnouncementDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI()
        setupViewModel()
        setupAnnouncementDetail()
    }

    //    Setup UI
    private fun setupUI() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        handleBackButton()
    }

    //    Setup View Model : AdminAnnouncementDetailViewModel
    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminAnnouncementDetailViewModel::class.java]
    }

    //    Setup Detail Announcement
    private fun setupAnnouncementDetail() {
        val announcementId = intent.getStringExtra("announcementId")

        if (announcementId != null) {
            viewModel.getAnnouncementDetail(announcementId)
            viewModel.announcementDetail.observe(this@AnnouncementDetailActivity) {
                it.onSuccess { announcement ->
                    binding.tvUsername.text = announcement.createdByName
                    binding.tvEmail.text = announcement.createdByEmail
                    binding.tvPost.text = announcement.announcement
                    binding.tvDate.text = CommonHelper.formatTimestamp(announcement.createdAt)
                    if (announcement.imageAnnouncement != "") {
                        binding.cvIvPost.visibility = View.VISIBLE
                    } else {
                        binding.cvIvPost.visibility = View.GONE
                    }
                    Glide.with(this@AnnouncementDetailActivity)
                        .load(announcement.imageAnnouncement)
                        .into(binding.ivPost)

                    Glide.with(this@AnnouncementDetailActivity)
                        .load(announcement.imageUser)
                        .into(binding.ivUserProfile)
                }.onFailure {
                    CommonHelper.showInformationFailedDialog(
                        this@AnnouncementDetailActivity,
                        getString(R.string.failed),
                        getString(R.string.no_announcement_description),
                        onOkButton = { finish() }
                    )
                }
            }
        } else {
            CommonHelper.showInformationFailedDialog(
                this@AnnouncementDetailActivity,
                getString(R.string.failed),
                getString(R.string.no_announcement_description),
                onOkButton = { finish() })
        }
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

}