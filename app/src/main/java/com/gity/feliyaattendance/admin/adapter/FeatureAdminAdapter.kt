package com.gity.feliyaattendance.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gity.feliyaattendance.databinding.ListItemAdminFeaturesBinding

class FeatureAdminAdapter(
    private val featureNames: Array<String>,
    private val featureIcons: Array<Int>,
    private val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<FeatureAdminAdapter.FeatureAdminViewHolder>() {

    inner class FeatureAdminViewHolder(val binding: ListItemAdminFeaturesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(featureName: String, featureIcon: Int) {
            binding.apply {
                tvFeature.text = featureName
                ivIcon.setImageResource(featureIcon)

                root.setOnClickListener {
                    onItemClicked(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FeatureAdminViewHolder {
        val binding =
            ListItemAdminFeaturesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeatureAdminViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FeatureAdminViewHolder,
        position: Int
    ) {
        holder.bind(featureNames[position], featureIcons[position])
    }

    override fun getItemCount(): Int = featureNames.size
}