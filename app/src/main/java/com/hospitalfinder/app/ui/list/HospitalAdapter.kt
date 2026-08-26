package com.hospitalfinder.app.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hospitalfinder.app.R
import com.hospitalfinder.app.databinding.ItemHospitalCardBinding
import com.hospitalfinder.app.model.Hospital

/**
 * Lightweight RecyclerView adapter for the hospital list.
 * Uses ListAdapter + DiffUtil so updates are cheap and scrolling stays smooth.
 *
 * Click handling is delegated to the caller via [onHospitalClick] rather
 * than starting an activity directly, so the owning fragment can launch
 * HospitalDetailsActivity through its own ActivityResultLauncher and react
 * to a requested tab switch on return.
 */
class HospitalAdapter(
    private val onHospitalClick: (Hospital) -> Unit
) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

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
        return HospitalViewHolder(binding, onHospitalClick)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    class HospitalViewHolder(
        private val binding: ItemHospitalCardBinding,
        private val onHospitalClick: (Hospital) -> Unit
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

            binding.imgHospital.setImageDrawable(null)
            binding.imgPlaceholderIcon.visibility = android.view.View.VISIBLE

            binding.root.setOnClickListener { onHospitalClick(hospital) }
        }
    }
}