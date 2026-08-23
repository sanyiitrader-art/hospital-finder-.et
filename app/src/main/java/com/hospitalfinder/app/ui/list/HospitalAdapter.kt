package com.hospitalfinder.app.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hospitalfinder.app.R
import com.hospitalfinder.app.databinding.ItemHospitalCardBinding
import com.hospitalfinder.app.model.Hospital

/**
 * Lightweight RecyclerView adapter for the hospital list.
 * Uses ListAdapter + DiffUtil so updates are cheap and scrolling stays smooth.
 */
class HospitalAdapter : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    private val differ = androidx.recyclerview.widget.AsyncListDiffer(
        this,
        object : DiffUtil.ItemCallback<Hospital>() {
            override fun areItemsTheSame(oldItem: Hospital, newItem: Hospital) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Hospital, newItem: Hospital) =
                oldItem == newItem
        }
    )

    fun submitList(hospitals: List<Hospital>) = differ.submitList(hospitals)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val binding = ItemHospitalCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HospitalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    class HospitalViewHolder(
        private val binding: ItemHospitalCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hospital: Hospital) {
            binding.txtHospitalName.text = hospital.name

            val context = binding.root.context
            if (hospital.isOpen) {
                binding.txtStatus.text = context.getString(R.string.status_open)
                binding.txtStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.status_open)
                )
                binding.statusDot.background.setTint(
                    ContextCompat.getColor(context, R.color.status_open)
                )
            } else {
                binding.txtStatus.text = context.getString(R.string.status_closed)
                binding.txtStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.status_closed)
                )
                binding.statusDot.background.setTint(
                    ContextCompat.getColor(context, R.color.status_closed)
                )
            }

            // No real image source is specified in current scope — show the
            // placeholder icon and keep the ImageView itself empty/transparent.
            binding.imgHospital.setImageDrawable(null)
            binding.imgPlaceholderIcon.visibility = android.view.View.VISIBLE
        }
    }
}