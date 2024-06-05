package com.example.freebite2.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.RecyclerItemNotificationBinding
import com.example.freebite2.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationsAdapter(
    private var notifications: MutableList<Notification>,
    private val databaseReference: DatabaseReference,
    private val context: Context
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: RecyclerItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = RecyclerItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    fun updateList(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.binding.notifi = notification
        val auth = FirebaseAuth.getInstance()

        val profileImageIv = holder.binding.profileImageIvNotif
        val senderName = holder.binding.usernameTv

        if (notification.type == "admin") {
            senderName.text = "Admin"
            profileImageIv.setImageResource(R.drawable.profile_holder_user)
        } else if (notification.type == "offer_taken") {
            val senderId = notification.senderId
            val usersReference = FirebaseDatabase.getInstance().getReference("Users").child(senderId!!)
            usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val profileImageUrl = dataSnapshot.child("profilePictureUrl").value.toString()
                        val nom = dataSnapshot.child("nom").value.toString()
                        val prenom = dataSnapshot.child("prenom").value.toString()
                        Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profile_holder_user)
                            .error(R.drawable.profile_holder_user)
                            .into(profileImageIv)
                        senderName.text = "$nom $prenom"
                    } else {
                        profileImageIv.setImageResource(R.drawable.profile_holder_user)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching user profile image: ${databaseError.message}")
                }
            })
        } else {
            Glide.with(context)
                .load(notification.profileImageUrl)
                .placeholder(R.drawable.profile_holder_user)
                .error(R.drawable.error_image)
                .into(profileImageIv)
        }

        if (notification.type == "admin") {
            profileImageIv.setImageResource(R.drawable.admin_pdp_54)
        }

        holder.binding.seenButton.setOnClickListener {
            if (notification.OfferID == null) {
                Log.e("NotificationsAdapter", "L'ID de l'offre est null")
                return@setOnClickListener
            }

          /*  databaseReference.child(notification.OfferID!!).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Offer taken!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to take offer. Try again.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("NotificationsAdapter", "Failed to remove offer", exception)
            }*/
        }
    }

    override fun getItemCount() = notifications.size
}
