package com.example.freebite2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.RecyclerItemBinding
import com.example.freebite2.model.OffreModel

class OffersAdapter(private val offers: List<OffreModel>, private val onOfferClickListener: OnOfferClickListener) : RecyclerView.Adapter<OffersAdapter.OfferViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerItemBinding.inflate(layoutInflater, parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(offers[position])
    }

    override fun getItemCount(): Int = offers.size

    inner class OfferViewHolder(private val binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: OffreModel) {
            binding.offre = offer
            binding.executePendingBindings()

            // Load the image using Glide
            offer.pictureUrl?.let { url ->
                Glide.with(binding.imageIv.context)
                    .load(url)
                    .into(binding.imageIv)
            }

            // Set click listener
            binding.root.setOnClickListener {
                onOfferClickListener.onOfferClick(offer)
            }
            // Set click listener for the more button
            binding.moreBtn.setOnClickListener { view ->
                // Create a PopupMenu
                val popup = PopupMenu(view.context, view)
                // Inflate the menu from xml
                popup.menuInflater.inflate(R.menu.offer_menu, popup.menu)
                // Set menu item click listener
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.edit_offer -> {
                            // Handle edit offer
                            onOfferClickListener.onEditOfferClick(offer)
                            true
                        }
                        R.id.delete_offer -> {
                            // Handle delete offer
                            onOfferClickListener.onDeleteOfferClick(offer)
                            true
                        }
                        else -> false
                    }
                }
                // Show the PopupMenu
                popup.show()
            }
        }

    }

    // Interface to handle item clicks
    interface OnOfferClickListener {
        fun onOfferClick(offer: OffreModel)
        fun onEditOfferClick(offer: OffreModel)
        fun onDeleteOfferClick(offer: OffreModel)
    }
}

