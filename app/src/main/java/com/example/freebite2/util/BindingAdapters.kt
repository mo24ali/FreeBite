package com.example.freebite2.util



import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.freebite2.R
import com.squareup.picasso.Picasso // Import Picasso library

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Picasso.get() // Use Picasso.get() to obtain the Picasso instance
            .load(imageUrl)
            .placeholder(R.drawable.placeholder) // Set a placeholder image
            .error(R.drawable.error_image) // Set an error image
            .into(view)
    } else {
        view.setImageResource(R.drawable.placeholder) // Set placeholder image if URL is null or empty
    }
}