package com.example.freebite2.viewmodel

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.model.OffreModel
import com.google.android.material.textfield.TextInputEditText

class OfferViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val titleTextView: TextInputEditText = view.findViewById(R.id.titleEditText)
    val detailsTextView: TextInputEditText = view.findViewById(R.id.descriptionEditText)
    val durationTextView: TextInputEditText = view.findViewById(R.id.durationEditText)
    val imageView: ImageView = view.findViewById(R.id.imageView)

    fun bind(offer: OffreModel) {
        titleTextView.setText(offer.nameoffre)
        detailsTextView.setText(offer.details)
        durationTextView.setText(offer.duration)
        Glide.with(imageView.context).load(offer.pictureUrl).into(imageView)
    }
}