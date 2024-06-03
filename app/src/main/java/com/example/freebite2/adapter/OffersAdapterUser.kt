package com.example.freebite2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.RecyclerItemUserBinding
import com.example.freebite2.model.OffreModel

class OffersAdapterUser(
    private val offers: List<OffreModel>,
    private val listener: OnOfferClickListener
) : RecyclerView.Adapter<OffersAdapterUser.OfferViewHolder>() {

    interface OnOfferClickListener {
        fun onOfferClick(offer: OffreModel)
        fun onEditOfferClick(offer: OffreModel)
        fun onDeleteOfferClick(offer: OffreModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecyclerItemUserBinding.inflate(inflater, parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val offer = offers[position]
        holder.bind(offer)
    }

    override fun getItemCount(): Int = offers.size

    inner class OfferViewHolder(private val binding: RecyclerItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: OffreModel) {
            binding.offre = offer
            binding.executePendingBindings()
            offer.pictureUrl?.let { url ->
                Glide.with(binding.imageIv.context)
                    .load(url)
                    .into(binding.imageIv)
            }
            // Définir un écouteur de clic sur l'élément entier
            binding.root.setOnClickListener {
                listener.onOfferClick(offer)
            }

            binding.moreBtnUser.setOnClickListener {
                showPopupMenu(it, offer)
            }

            itemView.setOnClickListener {
                listener.onOfferClick(offer)
            }
        }

        private fun showPopupMenu(view: View, offer: OffreModel) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.offer_menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit_offer -> {
                        listener.onEditOfferClick(offer)
                        true
                    }
                    R.id.delete_offer -> {
                        listener.onDeleteOfferClick(offer)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}
