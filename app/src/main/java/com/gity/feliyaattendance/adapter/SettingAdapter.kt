package com.gity.feliyaattendance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.data.model.Setting

class SettingAdapter(
    private val settingList: List<Setting>,
    private val onItemClick: (Setting) -> Unit
) : RecyclerView.Adapter<SettingAdapter.SettingViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SettingAdapter.SettingViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_settings, parent, false)
        return SettingViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingAdapter.SettingViewHolder, position: Int) {
        holder.bind(settingList[position])
    }

    override fun getItemCount(): Int {
        return settingList.size
    }

    inner class SettingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivSettingIcon: ImageView = itemView.findViewById(R.id.iv_setting_icon)
        private val tvSettingName: TextView = itemView.findViewById(R.id.tv_setting_name)

        fun bind(setting: Setting) {
            ivSettingIcon.setImageResource(setting.ivSetting)
            tvSettingName.text = setting.tvSetting

            itemView.setOnClickListener {
                onItemClick(setting)
            }
        }
    }
}