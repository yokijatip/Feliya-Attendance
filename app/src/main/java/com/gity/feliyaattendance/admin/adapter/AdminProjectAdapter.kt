package com.gity.feliyaattendance.admin.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.data.model.Project
import com.gity.feliyaattendance.databinding.ListItemAdminProjectBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.google.android.material.card.MaterialCardView

class AdminProjectAdapter(private val onProjectSelected: (Project) -> Unit) :
    androidx.recyclerview.widget.ListAdapter<Project, AdminProjectAdapter.ProjectViewHolder>(
        ProjectDiffCallback()
    ) {

    inner class ProjectViewHolder(private val binding: ListItemAdminProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(project: Project) {
            binding.tvProjectName.text = project.projectName
            binding.tvLocation.text = project.location
            binding.tvStartDate.text = CommonHelper.formatTimestamp(project.startDate)
            binding.tvEndDate.text = CommonHelper.formatTimestamp(project.endDate)
            itemView.setOnClickListener {
                project.projectId
                onProjectSelected(project)
            }
            Glide.with(itemView.context)
                .load(project.projectImage)
                .placeholder(R.drawable.iv_placeholder).into(binding.ivProjectImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAdminProjectBinding.inflate(inflater, parent, false)
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem.projectId == newItem.projectId
        }

        override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem == newItem
        }
    }

//    private fun setProjectStatus(status: String, cardView: MaterialCardView, textView: TextView) {
//        when (status) {
//            "Active" -> {
//                cardView.setBackgroundTintList(
//                    ColorStateList.valueOf(
//                        ContextCompat.getColor(
//                            cardView.context,
//                            R.color.status_approved_background
//                        )
//                    )
//                )
//                textView.setTextColor(
//                    ContextCompat.getColor(
//                        cardView.context,
//                        R.color.status_approved
//                    )
//                )
//            }
//
//            "Inactive" -> {
//                cardView.setBackgroundTintList(
//                    ColorStateList.valueOf(
//                        ContextCompat.getColor(
//                            cardView.context,
//                            R.color.status_rejected_background
//                        )
//                    )
//                )
//                textView.setTextColor(
//                    ContextCompat.getColor(
//                        cardView.context,
//                        R.color.status_rejected
//                    )
//                )
//            }
//
//            "Completed" -> {
//                cardView.setBackgroundTintList(
//                    ColorStateList.valueOf(
//                        ContextCompat.getColor(
//                            cardView.context,
//                            R.color.status_pending_background
//                        )
//                    )
//                )
//                textView.setTextColor(
//                    ContextCompat.getColor(
//                        cardView.context,
//                        R.color.status_pending
//                    )
//                )
//            }
//        }
//    }


}