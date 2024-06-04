package com.example.freebite2.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.model.Notification
import com.example.freebite2.model.User
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class NotificationsAdapter(private var notifications: MutableList<Notification>,
                           private val databaseReference: DatabaseReference,
                           private val context: Context
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    fun updateList(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        val auth = FirebaseAuth.getInstance()

        val profileImageIv = holder.view.findViewById<ShapeableImageView>(R.id.profileImageIvNotif)
        val usernameTv = holder.view.findViewById<TextView>(R.id.usernameTv)
        val messageTv = holder.view.findViewById<TextView>(R.id.messageTv)
        val senderTypeTv = holder.view.findViewById<TextView>(R.id.senderTypeTv)
        val dateNotif = holder.view.findViewById<TextView>(R.id.dateTv)

        val userCourant = User(uid = auth.currentUser?.uid ?: "")

        if(notification.senderId == auth.currentUser?.uid){
            userCourant.uid = auth.currentUser!!.uid
        }

        // Utilisez Glide pour charger l'image de profil
        Glide.with(context)
            .load(notification.profileImageUrl)
            .placeholder(R.drawable.profile_holder_user) // Image de remplacement
            .error(R.drawable.error_image) // Image en cas d'erreur
            .into(profileImageIv)

        if(notification.type == "admin"){
            profileImageIv.setImageResource(R.drawable.admin_pdp_54)
        } else {
            val imageUri = Uri.parse(userCourant.profilePictureUrl)
            profileImageIv.setImageURI(imageUri)
        }

        dateNotif.text = notification.timestamp
        usernameTv.text = notification.senderId
        messageTv.text = notification.message
        senderTypeTv.text = notification.type

        holder.view.findViewById<Button>(R.id.seenButton).setOnClickListener {
            // Supprimez l'offre de Firebase
            notification.OfferID?.let { it1 ->
                databaseReference.child(it1).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Offer taken!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to take offer. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun getItemCount() = notifications.size
}
