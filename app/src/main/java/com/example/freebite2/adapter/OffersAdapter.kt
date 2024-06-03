package com.example.freebite2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.databinding.RecyclerItemBinding
import com.example.freebite2.model.OffreModel

class OffersAdapter(private var offers: List<OffreModel>, private val onOfferClickListener: OnOfferClickListener) : RecyclerView.Adapter<OffersAdapter.OfferViewHolder>() {

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

            // Charger l'image en utilisant Glide
            offer.pictureUrl?.let { url ->
                Glide.with(binding.imageIv.context)
                    .load(url)
                    .into(binding.imageIv)
            }

            // Définir un écouteur de clic sur l'élément entier
            binding.root.setOnClickListener {
                onOfferClickListener.onOfferClick(offer)
            }

            // Définir un écouteur de clic pour le bouton "plus"
            /*binding.moreBtnUser.setOnClickListener { view ->
                // Créer un PopupMenu
                val popup = PopupMenu(view.context, view)
                // Gonfler le menu depuis le fichier XML
                popup.menuInflater.inflate(R.menu.offer_menu, popup.menu)
                // Définir un écouteur pour les clics sur les éléments du menu
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.edit_offer -> {
                            // Gérer l'édition de l'offre
                            onOfferClickListener.onEditOfferClick(offer)
                            true
                        }
                        R.id.delete_offer -> {
                            // Gérer la suppression de l'offre
                            onOfferClickListener.onDeleteOfferClick(offer)
                            true
                        }
                        else -> false
                    }
                }
                // Afficher le PopupMenu
                popup.show()
            }*/
        }
    }

    // Mettre à jour la liste des offres
    fun updateList(newOfferList: List<OffreModel>) {
        offers = newOfferList
        notifyDataSetChanged()
    }

    // Interface pour gérer les clics sur les éléments
    interface OnOfferClickListener {
        fun onOfferClick(offer: OffreModel)

    }

    companion object {
        fun notifyDataSetChanged(adapter: RecyclerView.Adapter<*>) {
            adapter.notifyDataSetChanged()
        }
    }

}
