package com.gity.feliyaattendance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.data.model.DetailWorkerMenu

class WorkerDetailAdapter(
    private val workerDetailMenuList: List<DetailWorkerMenu>,
    private val onItemClick: (DetailWorkerMenu) -> Unit
) : RecyclerView.Adapter<WorkerDetailAdapter.WorkerDetailViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WorkerDetailAdapter.WorkerDetailViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_settings, parent, false)
        return WorkerDetailViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WorkerDetailAdapter.WorkerDetailViewHolder,
        position: Int
    ) {
        holder.bind(workerDetailMenuList[position])
    }

    override fun getItemCount(): Int {
        return workerDetailMenuList.size
    }

    inner class WorkerDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivMenuIcon: ImageView = itemView.findViewById(R.id.iv_setting_icon)
        private val tvMenuName: TextView = itemView.findViewById(R.id.tv_setting_name)

        fun bind(workerDetailMenu: DetailWorkerMenu) {
            ivMenuIcon.setImageResource(workerDetailMenu.ivDetailWorkerMenu)
            tvMenuName.text = workerDetailMenu.tvDetailWorkerMenu

            itemView.setOnClickListener {
                onItemClick(workerDetailMenu)
            }
        }

    }

}