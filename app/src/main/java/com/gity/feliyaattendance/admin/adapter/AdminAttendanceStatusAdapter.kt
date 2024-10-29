package com.gity.feliyaattendance.admin.adapter

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.data.model.Attendance
import com.gity.feliyaattendance.databinding.ListItemAdminAttendanceBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore

class AdminAttendanceStatusAdapter(
    private val onAttendanceSelected: (Attendance) -> Unit,
) :
    ListAdapter<Attendance, AdminAttendanceStatusAdapter.AttendanceStatusViewHolder>(
        AttendanceStatusDiffCallback()
    ) {
    inner class AttendanceStatusViewHolder(private val binding: ListItemAdminAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(attendanceStatus: Attendance) {
            binding.apply {
                tvDate.text = CommonHelper.formatTimestamp(attendanceStatus.date)
                tvClockIn.text = CommonHelper.formatTimeOnly(attendanceStatus.clockInTime)
                tvClockOut.text = CommonHelper.formatTimeOnly(attendanceStatus.clockOutTime)
                tvTotalHours.text = attendanceStatus.totalHours.toString()
                tvStatus.text = attendanceStatus.status
                // Set default name terlebih dahulu
                tvWorkerName.text = "Loading..."

                getName(attendanceStatus.userId) { result ->
                    result.onSuccess { name ->
                        binding.tvWorkerName.text = name
                    }.onFailure { exception ->
                        binding.tvWorkerName.text = "Unknown Name"
                        Log.e("AttendanceStatusAdapter", "Error fetching name | Error : $exception")
                    }
                }
                setAttendanceStatus(attendanceStatus.status, cardStatus, tvStatus)
            }
            itemView.setOnClickListener {
                onAttendanceSelected(attendanceStatus)
            }
        }
    }

    private fun setAttendanceStatus(
        status: String,
        cardView: MaterialCardView,
        textView: TextView
    ) {
        when (status) {
            "pending" -> {
                cardView.setBackgroundTintList(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(cardView.context, R.color.status_pending_background)
                    )
                )
                textView.setTextColor(
                    ContextCompat.getColor(
                        cardView.context,
                        R.color.status_pending
                    )
                )
            }

            "rejected" -> {
                cardView.setBackgroundTintList(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(cardView.context, R.color.status_rejected_background)
                    )
                )
                textView.setTextColor(
                    ContextCompat.getColor(
                        cardView.context,
                        R.color.status_rejected
                    )
                )
            }

            "approved" -> {
                cardView.setBackgroundTintList(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(cardView.context, R.color.status_approved_background)
                    )
                )
                textView.setTextColor(
                    ContextCompat.getColor(
                        cardView.context,
                        R.color.status_approved
                    )
                )
            }
        }
    }

    class AttendanceStatusDiffCallback : DiffUtil.ItemCallback<Attendance>() {
        override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
            return oldItem.attendanceId == newItem.attendanceId
        }

        override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminAttendanceStatusAdapter.AttendanceStatusViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAdminAttendanceBinding.inflate(inflater, parent, false)
        return AttendanceStatusViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AdminAttendanceStatusAdapter.AttendanceStatusViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    private val nameCache = mutableMapOf<String, String>()

    private fun getName(userId: String, callback: (Result<String>) -> Unit) {
        // Cek cache terlebih dahulu
        nameCache[userId]?.let {
            callback(Result.success(it))
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists() && document.contains("name")) {
                    val name = document.getString("name") ?: "Unknown Name"
                    nameCache[userId] = name  // Simpan di cache
                    callback(Result.success(name))
                } else {
                    callback(Result.failure(Exception("Name field not found in document")))
                }
            }
            .addOnFailureListener { exception ->
                callback(Result.failure(exception))
            }
    }
}