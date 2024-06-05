/*package com.example.freebite2.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.DialogWarningBinding
import com.example.freebite2.databinding.RecyclerItemNotificationBinding
import com.example.freebite2.model.Notification
import com.example.freebite2.model.OffreModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime

class NotificationsAdapter(
    private var notifications: MutableList<Notification>,
    private val databaseReference: DatabaseReference,
    private val context: Context

) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: RecyclerItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)
    private lateinit var sender:String
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
        sender = notification.senderId

        val profileImageIv = holder.binding.profileImageIvNotif
        val senderName = holder.binding.usernameTv

        if (notification.type == "admin") {
            senderName.text = "Admin"
            profileImageIv.setImageResource(R.drawable.profile_holder_user)
        } else  {
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

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching user profile image: ${databaseError.message}")
                }
            })
        }

        if (notification.type == "admin") {
            profileImageIv.setImageResource(R.drawable.admin_pdp_54)
        }
        holder.binding.repondbtn.setOnClickListener {
            sendAcceptNotification(notification.OfferID)

        }

        holder.binding.accepteButton.setOnClickListener {
            if (notification.OfferID == null) {
                Log.e("NotificationsAdapter", "L'ID de l'offre est null")
                return@setOnClickListener
            }
            val warningMessage = "je reserve le pour vous"

            if (warningMessage.isNotEmpty()) {
                val notificationRef = FirebaseDatabase.getInstance().getReference("Notifications").child(notification.senderId.toString())
                val notificationId = notificationRef.push().key
                val notification = mapOf(
                    "title" to "reponce",
                    "message" to warningMessage,
                    "type" to "offer_taken",
                    "timestamp" to LocalDateTime.now().toString(),
                    "offreID" to notification.OfferID
                )
                if (notificationId != null) {
                    notificationRef.child(notificationId).setValue(notification)
                    Toast.makeText(context, "reponse envoyé avec succès", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(context, "Le message de Reponce ne peut pas être vide", Toast.LENGTH_SHORT).show()
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


  private fun showAcceptDialog(offerModel: OffreModel) {
      val dialogBinding = DialogWarningBinding.inflate(LayoutInflater.from(context))

      dialogBinding.userName.text = "${offerModel.nameoffre}"

      val dialog = AlertDialog.Builder(context)
          .setTitle("Envoyer un avertissement à l'utilisateur")
          .setView(dialogBinding.root)
          .setPositiveButton("Envoyer", null)
          .setNegativeButton("Annuler", null)
          .create()

      dialog.setOnShowListener {
          val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
          positiveButton.setTextColor(Color.BLACK)

          val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
          negativeButton.setTextColor(Color.GRAY)

          positiveButton.setOnClickListener {
              val warningMessage = dialogBinding.warningMessage.text.toString()

              if (warningMessage.isNotEmpty()) {
                  val notificationRef = FirebaseDatabase.getInstance().getReference("Notifications").child(sender)
                  val notificationId = notificationRef.push().key
                  val notification = mapOf(
                      "title" to "reponce",
                      "message" to warningMessage,
                      "type" to "offer_taken",
                      "timestamp" to LocalDateTime.now().toString(),
                      "offreID" to offerModel.offerID.toString()
                  )
                  if (notificationId != null) {
                      notificationRef.child(notificationId).setValue(notification)
                      Toast.makeText(context, "reponse envoyé avec succès", Toast.LENGTH_SHORT).show()
                  }
                  dialog.dismiss()
              } else {
                  Toast.makeText(context, "Le message de Reponce ne peut pas être vide", Toast.LENGTH_SHORT).show()
              }
          }
      }

      dialog.show()
  }
    private fun sendAcceptNotification(offerID: String?) {
        if (offerID == null) {
            Log.e("NotificationsAdapter", "L'ID de l'offre est null")
            return
        }

        val offerReference = FirebaseDatabase.getInstance().getReference("offres").child(offerID)

        offerReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val offerModel = dataSnapshot.getValue(OffreModel::class.java)
                    if (offerModel != null) {
                        showAcceptDialog(offerModel)
                    } else {
                        Log.e("NotificationsAdapter", "Failed to parse offer model")
                    }
                } else {
                    Log.e("NotificationsAdapter", "Offer does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("NotificationsAdapter", "Error fetching offer: ${databaseError.message}")
            }
        })
    }
    override fun getItemCount() = notifications.size
}

////////////////

package com.example.freebite2.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.DialogWarningBinding
import com.example.freebite2.databinding.RecyclerItemNotificationBinding
import com.example.freebite2.model.Notification
import com.example.freebite2.model.OffreModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

        val profileImageIv = holder.binding.profileImageIvNotif
        val senderName = holder.binding.usernameTv

        if (notification.type == "admin") {
            senderName.text = "Admin"
            profileImageIv.setImageResource(R.drawable.profile_holder_user)
        } else {
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
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching user profile image: ${databaseError.message}")
                }
            })
        }

        if (notification.type == "admin") {
            profileImageIv.setImageResource(R.drawable.admin_pdp_54)
        }

        holder.binding.repondbtn.setOnClickListener {
            sendAcceptNotification(notification.OfferID, notification.senderId)
        }

        holder.binding.accepteButton.setOnClickListener {
            if (notification.OfferID == null) {
                Log.e("NotificationsAdapter", "L'ID de l'offre est null")
                return@setOnClickListener
            }
            val warningMessage = "je réserve le pour vous"

            if (warningMessage.isNotEmpty()) {
                val notificationRef = FirebaseDatabase.getInstance().getReference("Notifications").child(notification.senderId)
                val notificationId = notificationRef.push().key

               val time= LocalDate.now().toString() + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                val newNotification = mapOf(
                    "title" to "réponse",
                    "message" to warningMessage,
                    "type" to "offer_taken",
                    "timestamp" to time,
                    "offreID" to notification.OfferID
                )
                if (notificationId != null) {
                    notificationRef.child(notificationId).setValue(newNotification)
                    Toast.makeText(context, "réponse envoyée avec succès", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Le message de réponse ne peut pas être vide", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAcceptDialog(offerModel: OffreModel, senderId: String) {
        val dialogBinding = DialogWarningBinding.inflate(LayoutInflater.from(context))
        dialogBinding.userName.text = offerModel.nameoffre

        val dialog = AlertDialog.Builder(context)
            .setTitle("Envoyer un reponce ")
            .setView(dialogBinding.root)
            .setPositiveButton("Envoyer", null)
            .setNegativeButton("Annuler", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.BLACK)

            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(Color.GRAY)

            positiveButton.setOnClickListener {
                val warningMessage = dialogBinding.warningMessage.text.toString()

                if (warningMessage.isNotEmpty()) {
                    val notificationRef = FirebaseDatabase.getInstance().getReference("Notifications").child(senderId)
                    val notificationId = notificationRef.push().key
                    val time= LocalDate.now().toString() + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    val notification = mapOf(
                        "title" to "reponce",
                        "message" to warningMessage,
                        "type" to "admin",
                        "timestamp" to time,
                        "offreID" to offerModel.offerID.toString()
                    )
                    if (notificationId != null) {
                        notificationRef.child(notificationId).setValue(notification)
                        Toast.makeText(context, "Avertissement envoyé avec succès", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Le message d'avertissement ne peut pas être vide", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun sendAcceptNotification(offerID: String?, senderId: String) {
        if (offerID == null) {
            Log.e("NotificationsAdapter", "L'ID de l'offre est null")
            return
        }

        val offerReference = FirebaseDatabase.getInstance().getReference("offres").child(offerID)
        offerReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val offerModel = dataSnapshot.getValue(OffreModel::class.java)
                    if (offerModel != null) {
                        showAcceptDialog(offerModel, senderId)
                    } else {
                        Log.e("NotificationsAdapter", "Failed to parse offer model")
                    }
                } else {
                    Log.e("NotificationsAdapter", "Offer does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("NotificationsAdapter", "Error fetching offer: ${databaseError.message}")
            }
        })
    }

    override fun getItemCount() = notifications.size
}


 */
package com.example.freebite2.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.DialogWarningBinding
import com.example.freebite2.databinding.RecyclerItemNotificationBinding
import com.example.freebite2.model.Notification
import com.example.freebite2.model.OffreModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

        val profileImageIv = holder.binding.profileImageIvNotif
        val senderName = holder.binding.usernameTv

        if (notification.type == "admin") {
            senderName.text = "Admin"
            profileImageIv.setImageResource(R.drawable.admin_pdp_54)
        } else {
            val senderId = notification.senderId
            val usersReference = FirebaseDatabase.getInstance().getReference("Users").child(senderId!!)
            usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val profileImageUrl = dataSnapshot.child("profilePictureUrl").value.toString()
                        val nom = dataSnapshot.child("nom").value.toString() ?: "inconnu"
                        val prenom = dataSnapshot.child("prenom").value.toString() ?: "inconnu"
                        Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profile_holder_user)
                            .error(R.drawable.profile_holder_user)
                            .into(profileImageIv)
                        senderName.text = "$nom $prenom"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching user profile image: ${databaseError.message}")
                }
            })
        }

        holder.binding.repondbtn.setOnClickListener {
            sendResponseNotification(notification.OfferID, notification.senderId)
        }

        holder.binding.accepteButton.setOnClickListener {
            sendAcceptNotification(notification.OfferID, notification.senderId)
        }
    }

    private fun sendAcceptNotification(offerID: String?, senderId: String) {
        if (offerID == null) {
            Log.e("NotificationsAdapter", "L'ID de l'offre est null")
            return
        }

        val warningMessage = "je réserve le pour vous"
        if (warningMessage.isNotEmpty()) {
            val notificationRef = FirebaseDatabase.getInstance().getReference("Notifications").child(senderId)
            val notificationId = notificationRef.push().key
            val timestamp = LocalDate.now().toString() + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val newNotification = mapOf(
                "title" to "réponse",
                "message" to warningMessage,
                "type" to "offer_taken",
                "senderId" to FirebaseAuth.getInstance().currentUser?.uid,
                "timestamp" to timestamp,
                "offreID" to offerID
            )
            if (notificationId != null) {
                notificationRef.child(notificationId).setValue(newNotification)
                Toast.makeText(context, "réponse envoyée avec succès", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Le message de réponse ne peut pas être vide", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendResponseNotification(offerID: String?, senderId: String) {
        if (offerID == null) {
            Log.e("NotificationsAdapter", "L'ID de l'offre est null")
            return
        }

        val offerReference = FirebaseDatabase.getInstance().getReference("offres").child(offerID)
        offerReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val offerModel = dataSnapshot.getValue(OffreModel::class.java)
                    if (offerModel != null) {
                        showAcceptDialog(offerModel, senderId)
                    } else {
                        Log.e("NotificationsAdapter", "Failed to parse offer model")
                    }
                } else {
                    Log.e("NotificationsAdapter", "Offer does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("NotificationsAdapter", "Error fetching offer: ${databaseError.message}")
            }
        })
    }

    private fun showAcceptDialog(offerModel: OffreModel, senderId: String) {
        val dialogBinding = DialogWarningBinding.inflate(LayoutInflater.from(context))
        dialogBinding.userName.text = offerModel.nameoffre

        val dialog = AlertDialog.Builder(context)
            .setTitle("Envoyer un reponce ")
            .setView(dialogBinding.root)
            .setPositiveButton("Envoyer", null)
            .setNegativeButton("Annuler", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.BLACK)

            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(Color.GRAY)

            positiveButton.setOnClickListener {
                val warningMessage = dialogBinding.warningMessage.text.toString()

                if (warningMessage.isNotEmpty()) {
                    val notificationRef = FirebaseDatabase.getInstance().getReference("Notifications").child(senderId)
                    val notificationId = notificationRef.push().key
                    val timestamp = LocalDate.now().toString() + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    val notification = mapOf(
                        "title" to "réponse",
                        "message" to warningMessage,
                        "type" to "offer_taken",
                        "timestamp" to timestamp,
                        "senderId" to FirebaseAuth.getInstance().currentUser?.uid,
                        "offreID" to offerModel.offerID.toString()
                    )
                    if (notificationId != null) {
                        notificationRef.child(notificationId).setValue(notification)
                        Toast.makeText(context, "reponse envoyé avec succès", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Le message ne peut pas être vide", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    override fun getItemCount() = notifications.size
}
