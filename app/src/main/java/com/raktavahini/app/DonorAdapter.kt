package com.raktavahini.app

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raktavahini.app.databinding.ItemDonorCardBinding

class DonorAdapter(private val donors: List<Donor>) :
    RecyclerView.Adapter<DonorAdapter.DonorViewHolder>() {

    inner class DonorViewHolder(val binding: ItemDonorCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val binding = ItemDonorCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DonorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        val donor = donors[position]
        val binding = holder.binding

        binding.tvBloodGroupBadge.text = donor.bloodGroup
        binding.tvDonorName.text = donor.name
        binding.tvDonorCity.text = "📍 ${donor.city}"

        // Availability status badge
        if (donor.isAvailable) {
            binding.tvAvailableBadge.text = "● Available"
            binding.tvAvailableBadge.setTextColor(
                holder.itemView.context.getColor(R.color.eligible_green))
        } else {
            binding.tvAvailableBadge.text = "● Unavailable"
            binding.tvAvailableBadge.setTextColor(
                holder.itemView.context.getColor(R.color.ineligible_grey))
        }

        // PRIVACY-SAFE: phone goes to system dialer only — never shown on screen
        binding.btnCallDonor.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${donor.phone}")
            }
            holder.itemView.context.startActivity(callIntent)
        }
    }

    override fun getItemCount() = donors.size
}