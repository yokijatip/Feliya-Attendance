package com.gity.feliyaattendance.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.data.model.Announcement
import com.gity.feliyaattendance.databinding.ListItemAdminAnnouncementBinding
import com.gity.feliyaattendance.helper.CommonHelper

class AdminAnnouncementAdapter(private val onAnnouncementSelected: (Announcement) -> Unit) :
    ListAdapter<Announcement, AdminAnnouncementAdapter.AnnouncementViewHolder>(
        AnnouncementDiffCallback()
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminAnnouncementAdapter.AnnouncementViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAdminAnnouncementBinding.inflate(inflater, parent, false)
        return AnnouncementViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AdminAnnouncementAdapter.AnnouncementViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class AnnouncementViewHolder(private val binding: ListItemAdminAnnouncementBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(announcement: Announcement) {
            binding.apply {
                tvUsername.text = announcement.createdByName
                tvEmail.text = announcement.createdByEmail
                tvDate.text = CommonHelper.formatTimestamp(announcement.createdAt)
                tvPost.text = announcement.announcement

                if (announcement.imageAnnouncement != "") {
                    cvIvPost.visibility = View.VISIBLE
                } else {
                    cvIvPost.visibility = View.GONE
                }

                Glide.with(itemView.context)
                    .load(announcement.imageAnnouncement)
                    .into(binding.ivPost)

                Glide.with(itemView.context)
                    .load(announcement.imageUser)
                    .placeholder(R.drawable.worker_profile_placeholder).into(binding.ivUserProfile)

                itemView.setOnClickListener {
                    onAnnouncementSelected(announcement)
                }
            }
        }
    }

    class AnnouncementDiffCallback : DiffUtil.ItemCallback<Announcement>() {
        override fun areItemsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            return oldItem == newItem
        }

    }
}