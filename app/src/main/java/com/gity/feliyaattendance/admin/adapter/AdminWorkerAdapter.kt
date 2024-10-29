package com.gity.feliyaattendance.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.data.model.Worker
import com.gity.feliyaattendance.databinding.ListItemWorkerBinding

class AdminWorkerAdapter(private val onWorkerSelected: (Worker) -> Unit) :
    ListAdapter<Worker, AdminWorkerAdapter.WorkerViewHolder>(WorkerDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AdminWorkerAdapter.WorkerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemWorkerBinding.inflate(inflater, parent, false)
        return WorkerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminWorkerAdapter.WorkerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkerViewHolder(private val binding: ListItemWorkerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(worker: Worker) {
            binding.apply {
                tvWorkerName.text = worker.name
                Glide.with(itemView.context).load(worker.profileImageUrl)
                    .placeholder(R.drawable.worker_profile_placeholder).into(ivWorkerImageProfile)

                itemView.setOnClickListener {
                    onWorkerSelected(worker)
                }
            }
        }
    }

    class WorkerDiffCallback : DiffUtil.ItemCallback<Worker>() {
        override fun areItemsTheSame(oldItem: Worker, newItem: Worker): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Worker, newItem: Worker): Boolean {
            return oldItem == newItem
        }

    }
}