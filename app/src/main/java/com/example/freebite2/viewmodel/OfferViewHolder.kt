package com.example.freebite2.viewmodel

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.model.OffreModel

class OfferViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private lateinit var binding:
    val titleTextView: TextView = view.findViewById(R.id.titleTextView)
    val detailsTextView: TextView = view.findViewById(R.id.detailsTextView)
    val durationTextView: TextView = view.findViewById(R.id.durationTextView)
    val imageView: ImageView = view.findViewById(R.id.imageView)

    fun bind(offer: OffreModel) {
        titleTextView.text = offer.nameoffre
        detailsTextView.text = offer.details
        durationTextView.text = offer.duration
        Glide.with(imageView.context).load(offer.pictureUrl).into(imageView)
    }
}