package com.example.freebite2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.AdminPostItemBinding
import com.example.freebite2.model.OffreModel

class OffersAdapterAdmin(
    private val offers: List<OffreModel>,
    private val listener: OnOfferClickListener
) : RecyclerView.Adapter<OffersAdapterAdmin.OfferViewHolder>() {

    interface OnOfferClickListener {
        fun onOfferClick(offer: OffreModel)
        fun onWarningOfferClick(offer: OffreModel)
        fun onDeleteOfferClick(offer: OffreModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdminPostItemBinding.inflate(inflater, parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val offer = offers[position]
        holder.bind(offer)
    }

    override fun getItemCount(): Int = offers.size

    inner class OfferViewHolder(private val binding: AdminPostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: OffreModel) {
            binding.offre = offer
            binding.executePendingBindings()

            // Charge l'image avec Glide
            offer.pictureUrl?.let { url ->
                Glide.with(binding.imageIv.context)
                    .load(url)
                    .into(binding.imageIv)
            }

            // Définit un écouteur de clic sur l'élément entier
            binding.root.setOnClickListener {
                listener.onOfferClick(offer)
            }

            // Gère le clic sur le bouton "Plus" pour afficher le menu contextuel
            binding.moreBtnUser.setOnClickListener {
                showPopupMenu(it, offer)
            }
        }

        // Méthode showPopupMenu dans OfferViewHolder
        private fun showPopupMenu(view: View, offer: OffreModel) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.edit_delete_menu_admin, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        listener.onWarningOfferClick(offer)
                        true
                    }
                    R.id.action_delete -> {
                        listener.onDeleteOfferClick(offer)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
}
