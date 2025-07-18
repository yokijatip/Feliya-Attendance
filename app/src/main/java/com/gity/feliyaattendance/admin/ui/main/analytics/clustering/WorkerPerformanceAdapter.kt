package com.gity.feliyaattendance.admin.ui.main.analytics.clustering

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.data.model.WorkerPerformanceData
import com.gity.feliyaattendance.databinding.ItemWorkerPerformanceBinding

class WorkerPerformanceAdapter(
    private val onItemClick: (WorkerPerformanceData) -> Unit
) : ListAdapter<WorkerPerformanceData, WorkerPerformanceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkerPerformanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemWorkerPerformanceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(worker: WorkerPerformanceData) {
            binding.apply {
                tvWorkerName.text = worker.name
                tvWorkerId.text = "ID: ${worker.workerId}"
                tvPerformanceLabel.text = worker.performanceLabel
                tvAttendanceRate.text = String.format("%.1f%%", worker.attendanceRate)
                tvWorkHours.text = String.format("%.1f hrs", worker.avgWorkHours)
                tvPunctuality.text = String.format("%.1f%%", worker.punctualityScore)
                tvConsistency.text = String.format("%.1f%%", worker.consistencyScore)
                tvConfidence.text = String.format("%.1f%%", worker.confidence * 100)

                // Set performance label color
                val (textColor, backgroundColor) = when (worker.performanceLabel) {
                    "High Performer" -> Pair(
                        ContextCompat.getColor(root.context, R.color.high_performer_text),
                        ContextCompat.getColor(root.context, R.color.high_performer_bg)
                    )

                    "Medium Performer" -> Pair(
                        ContextCompat.getColor(root.context, R.color.medium_performer_text),
                        ContextCompat.getColor(root.context, R.color.medium_performer_bg)
                    )

                    "Low Performer" -> Pair(
                        ContextCompat.getColor(root.context, R.color.low_performer_text),
                        ContextCompat.getColor(root.context, R.color.low_performer_bg)
                    )

                    else -> Pair(
                        ContextCompat.getColor(root.context, R.color.default_text),
                        ContextCompat.getColor(root.context, R.color.default_bg)
                    )
                }

                tvPerformanceLabel.setTextColor(textColor)
                tvPerformanceLabel.setBackgroundColor(backgroundColor)

                // Set click listener
                root.setOnClickListener {
                    onItemClick(worker)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<WorkerPerformanceData>() {
        override fun areItemsTheSame(
            oldItem: WorkerPerformanceData,
            newItem: WorkerPerformanceData
        ): Boolean = oldItem.userId == newItem.userId

        override fun areContentsTheSame(
            oldItem: WorkerPerformanceData,
            newItem: WorkerPerformanceData
        ): Boolean = oldItem == newItem
    }
}