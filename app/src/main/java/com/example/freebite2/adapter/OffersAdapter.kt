package com.example.freebite2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.freebite2.databinding.RecyclerItemBinding
import com.example.freebite2.model.OffreModel
import com.google.firebase.storage.FirebaseStorage


class OffersAdapter(private val offers: List<OffreModel>) : RecyclerView.Adapter<OffersAdapter.OfferViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerItemBinding.inflate(layoutInflater, parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(offers[position])
    }

    override fun getItemCount(): Int = offers.size

    /*inner class OfferViewHolder(private val binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: OffreModel) {
            binding.offre = offer
            binding.executePendingBindings()

            // Load image from URL into ImageView using Glide
            val pictureUrl = offer.pictureUrl

            if (!pictureUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(pictureUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.imageIv)
            }
        }
    }*/
    /*inner class OfferViewHolder(private val binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(offer: OffreModel) {
            binding.offre = offer
            binding.executePendingBindings()

            // Load image from Firebase Storage into ImageView using Glide
            val pictureUrl = offer.pictureUrl

            binding.imageIv.post{
                val width = binding.imageIv.width
                val height = binding.imageIv.height

                if (!pictureUrl.isNullOrEmpty()) {
                    val storageReference = FirebaseStorage.getInstance().getReference(pictureUrl)
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(binding.root.context)
                            .load(uri)
                            .override(width, height) // use the width and height of the ImageView
                            .placeholder(R.drawable.placeholder) // replace with your placeholder image
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.imageIv)
                    }
                }
            }
        }
    }*/
    inner class OfferViewHolder(private val binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(offer: OffreModel) {
            binding.offre = offer
            binding.executePendingBindings()
        }
    }
    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(view.context)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            }
        }
    }
}