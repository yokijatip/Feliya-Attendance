package com.gity.feliyaattendance.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.data.model.AdminMenu

class MenuAdminAdapter(
    private val menuList: List<AdminMenu>,
    private val onMenuClick: (AdminMenu) -> Unit
) : RecyclerView.Adapter<MenuAdminAdapter.MenuAdminViewHolder>() {

    inner class MenuAdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val menuName: TextView = itemView.findViewById(R.id.tv_admin_menu)
        private val menuIcon: ImageView = itemView.findViewById(R.id.iv_admin_menu)

        fun bind(menu: AdminMenu) {
            menuName.text = menu.name
            menuIcon.setImageResource(menu.icon)

            itemView.setOnClickListener { onMenuClick(menu) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MenuAdminAdapter.MenuAdminViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_admin_menu, parent, false)
        return MenuAdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuAdminAdapter.MenuAdminViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

}