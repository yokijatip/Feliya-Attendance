package com.gity.feliyaattendance.admin.ui.main.detail.announcement

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAdminAnnouncementDetailBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminAnnouncementDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAnnouncementDetailBinding
    private lateinit var viewModel: AdminAnnouncementDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminAnnouncementDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI()
        setupViewModel()
        setupAnnouncementDetail()
        setupMoreButton()
        observerDeleteAnnouncement()

    }

    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val viewModelFactory = ViewModelFactory(repository)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[AdminAnnouncementDetailViewModel::class.java]
    }

    private fun setupAnnouncementDetail() {
        val announcementId = intent.getStringExtra("announcementId")

        if (announcementId != null) {
            viewModel.getAnnouncementDetail(announcementId)
            viewModel.announcementDetail.observe(this@AdminAnnouncementDetailActivity) {
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
                    Glide.with(this@AdminAnnouncementDetailActivity)
                        .load(announcement.imageAnnouncement)
                        .into(binding.ivPost)

                    Glide.with(this@AdminAnnouncementDetailActivity)
                        .load(announcement.imageUser)
                        .into(binding.ivUserProfile)
                }.onFailure {
                    CommonHelper.showInformationFailedDialog(
                        this@AdminAnnouncementDetailActivity,
                        getString(R.string.failed),
                        getString(R.string.no_announcement_description),
                        onOkButton = { finish() }
                    )
                }
            }
        } else {
            CommonHelper.showInformationFailedDialog(
                this@AdminAnnouncementDetailActivity,
                getString(R.string.failed),
                getString(R.string.no_announcement_description),
                onOkButton = { finish() })
        }
    }

    private fun setupMoreButton() {
        binding.btnMore.setOnClickListener {
            showDialogImage()
        }
    }

    //    Show Dialog Image
    private fun showDialogImage() {
        val dialog = Dialog(this@AdminAnnouncementDetailActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.custom_dialog_menu_admin_announcement_detail)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_delete).setOnClickListener {
            CommonHelper.showConfirmationDialog(
                this@AdminAnnouncementDetailActivity,
                getString(R.string.delete_announcement),
                getString(R.string.delete_announcement_description),
                onPositiveClick = {
                    val announcementId = intent.getStringExtra("announcementId")
                    if (announcementId != null) {
                        viewModel.deleteAnnouncement(announcementId)
                    } else {
                        CommonHelper.showInformationFailedDialog(
                            this@AdminAnnouncementDetailActivity,
                            getString(R.string.failed),
                            getString(R.string.no_announcement_description),
                            onOkButton = { finish() })
                    }
                }
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun observerDeleteAnnouncement() {
        viewModel.deleteAnnouncement.observe(this@AdminAnnouncementDetailActivity) {
            it.onSuccess {
                CommonHelper.showInformationSuccessDialog(
                    this@AdminAnnouncementDetailActivity,
                    getString(R.string.successfully),
                    getString(R.string.delete_announcement_success),
                    onOkButton = { finish() }
                )
            }.onFailure {
                CommonHelper.showInformationFailedDialog(
                    this@AdminAnnouncementDetailActivity,
                    getString(R.string.failed),
                    getString(R.string.process_failed),
                )
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
    }

}