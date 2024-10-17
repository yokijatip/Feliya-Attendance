package com.gity.feliyaattendance.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.data.model.Attendance
import com.gity.feliyaattendance.databinding.ListItemAttendanceBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.google.android.material.card.MaterialCardView

class AttendanceAdapter(private val onAttendanceSelected: (Attendance) -> Unit) :
    ListAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder>(AttendanceDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttendanceAdapter.AttendanceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAttendanceBinding.inflate(inflater, parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceAdapter.AttendanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AttendanceViewHolder(private val binding: ListItemAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(attendance: Attendance) {
            binding.apply {
                tvDate.text = CommonHelper.formatTimestamp(attendance.date)
                tvClockIn.text = CommonHelper.formatTimeOnly(attendance.clockInTime)
                tvClockOut.text = CommonHelper.formatTimeOnly(attendance.clockOutTime)
                tvTotalHours.text = attendance.workHours.toString()
                tvStatus.text = attendance.status

                itemView.setOnClickListener {
                    onAttendanceSelected(attendance)
                }

                setAttendanceStatus(attendance.status, cardStatus, tvStatus)
            }
        }
    }

    class AttendanceDiffCallback : DiffUtil.ItemCallback<Attendance>() {
        override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
            return oldItem.attendanceId == newItem.attendanceId
        }

        override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
            return oldItem == newItem
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


}