package com.example.freebite2.adapter

import android.content.Context
import android.util.Log
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
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

        if (notification.type == "admin") {
            profileImageIv.setImageResource(R.drawable.profile_holder_user)
        } else if (notification.type == "offer_taken") {
            // Ici, vous devez récupérer l'URL de l'image de profil de l'utilisateur qui a envoyé la notification
            // Pour cela, vous pouvez utiliser une requête Firebase pour obtenir l'URL de l'image de profil à partir de l'ID de l'expéditeur
            val senderId = notification.senderId
            val usersReference = FirebaseDatabase.getInstance().getReference("Users").child(senderId!!)
            usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val profileImageUrl = dataSnapshot.child("profileImage").value.toString()
                        Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profile_holder_user) // Image de remplacement
                            .error(R.drawable.error_image) // Image en cas d'erreur
                            .into(profileImageIv)
                    } else {
                        // Handle case where user information does not exist
                        profileImageIv.setImageResource(R.drawable.profile_holder_user)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Log.e("Firebase", "Error fetching user profile image: ${databaseError.message}")
                }
            })
        } else {
            // Pour tous les autres types de notifications, vous pouvez charger l'image de profil à partir de l'URL stockée dans l'objet de notification
            Glide.with(context)
                .load(notification.profileImageUrl)
                .placeholder(R.drawable.profile_holder_user) // Image de remplacement
                .error(R.drawable.error_image) // Image en cas d'erreur
                .into(profileImageIv)
        }

        if(notification.type == "admin"){
            profileImageIv.setImageResource(R.drawable.admin_pdp_54)
        }

        dateNotif.text = notification.timestamp.toString()
        usernameTv.text = notification.senderId
        messageTv.text = notification.message
        senderTypeTv.text = notification.type

        holder.view.findViewById<Button>(R.id.seenButton).setOnClickListener {
            // Vérifiez si OfferID est null
            if (notification.OfferID == null) {
                Log.e("NotificationsAdapter", "L'ID de l'offre est null")
                return@setOnClickListener
            }

            // Supprimez l'offre de Firebase
            databaseReference.child(notification.OfferID!!).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Offer taken!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to take offer. Try again.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("NotificationsAdapter", "Failed to remove offer", exception)
            }
        }
    }

    override fun getItemCount() = notifications.size
}