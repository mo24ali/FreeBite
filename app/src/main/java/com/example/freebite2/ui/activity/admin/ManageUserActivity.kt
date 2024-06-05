package com.example.freebite2.ui.activity.admin

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.adapter.UsersAdapter
import com.example.freebite2.databinding.ActivityManageUserBinding
import com.example.freebite2.databinding.DialogUserInfoBinding
import com.example.freebite2.databinding.DialogWarningBinding
import com.example.freebite2.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate

class ManageUserActivity : AppCompatActivity(), UsersAdapter.OnUserClickListener {

    private lateinit var binding: ActivityManageUserBinding
    private var usersAdapter: UsersAdapter? = null
    private var userList: MutableList<User>? = null
    private var database: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userList = mutableListOf()
        usersAdapter = UsersAdapter(userList!!, this)

        setupRecyclerView()
        setupSearchView()
        fetchUsersFromDatabase()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = usersAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    filterUsersByQuery(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filterUsersByQuery(newText)
                }
                return false
            }
        })
    }

    private fun fetchUsersFromDatabase() {
        database = FirebaseDatabase.getInstance().getReference("Users")
        database?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList?.clear()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        // Set the uid of the user to the key of the dataSnapshot
                        user.uid = dataSnapshot.key.toString()
                        userList?.add(user)
                    }
                }
                usersAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", error.message)
            }
        })
    }

    private fun filterUsersByQuery(query: String) {
        val filteredList = userList?.filter {
            it.nom.contains(query, ignoreCase = true) ||
                    it.prenom.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        } ?: listOf()
        usersAdapter?.updateList(filteredList)
    }

    override fun onUserClick(user: User) {
        showUserInfoDialog(user)
    }

    private fun showUserInfoDialog(user: User) {
        val dialogBinding = DialogUserInfoBinding.inflate(LayoutInflater.from(this))

        val userName = "${user.nom ?: ""} ${user.prenom ?: ""}".trim()
        dialogBinding.userName.text = if (userName.isNotEmpty()) userName else "Nom inconnu"

        dialogBinding.userEmail.text = user.email ?: "Email inconnu"

        val profileUrl = user.profilePictureUrl ?: ""
        Glide.with(this)
            .load(profileUrl)
            .placeholder(R.drawable.profile_holder_user)
            .error(R.drawable.profile_holder_user)
            .fallback(R.drawable.profile_holder_user)
            .into(dialogBinding.userImage)

        AlertDialog.Builder(this)
            .setTitle("Informations de l'utilisateur")
            .setView(dialogBinding.root)
            .setPositiveButton("OK", null)
            .show()
    }

    fun sendWarningToUser(user: User) {
        val dialogBinding = DialogWarningBinding.inflate(LayoutInflater.from(this))

        dialogBinding.userName.text = "${user.nom} ${user.prenom}"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Envoyer un avertissement à l'utilisateur")
            .setView(dialogBinding.root)
            .setPositiveButton("Envoyer", null) // Set null here
            .setNegativeButton("Annuler", null)
            .create()

        dialog.setOnShowListener {
            // Change the color of the positive button
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.BLACK) // Change this to your desired color

            // Change the color of the negative button
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(Color.GRAY) // Change this to your desired color

            // Set the click listener for the positive button here
            positiveButton.setOnClickListener {
                val warningMessage = dialogBinding.warningMessage.text.toString()

            // Create a new notification in Firebase Database
                val notificationRef = FirebaseDatabase.getInstance().getReference("Notifications").child(user.uid)
                val notificationId = notificationRef.push().key
                val notification = mapOf(
                    "title" to "Avertissement",
                    "body" to warningMessage,
                    "type" to "admin",
                    "timestamp" to LocalDate.now().toString()
                )
                if (notificationId != null) {
                    notificationRef.child(notificationId).setValue(notification)
                    Toast.makeText(this, "Avertissement envoyé avec succès", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }
    override fun onSendWarningToUserClick(user: User) {
        sendWarningToUser(user)
    }

    /*override fun onDeleteUserClick(user: User) {
        if (user.uid.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)
            val offersRef = FirebaseDatabase.getInstance().getReference("offres").orderByChild("providerID").equalTo(user.uid)

            // Suppression de l'utilisateur
            userRef.removeValue().addOnCompleteListener { userDeletionTask ->
                if (userDeletionTask.isSuccessful) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    firebaseUser?.delete()?.addOnCompleteListener { authDeleteTask ->
                        if (authDeleteTask.isSuccessful) {
                            Toast.makeText(this, "Utilisateur supprimé avec succès", Toast.LENGTH_SHORT).show()
                            Log.d("onDeleteUserClick", "Utilisateur supprimé avec succès")
                            userList?.remove(user)
                            usersAdapter?.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this, "Erreur lors de la suppression de l'utilisateur de l'authentification", Toast.LENGTH_SHORT).show()
                            Log.e("onDeleteUserClick", "Erreur lors de la suppression de l'utilisateur de l'authentification", authDeleteTask.exception)
                        }
                    }
                    // Suppression des offres de l'utilisateur
                    offersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val tasks = mutableListOf<Task<Void>>()
                            for (offerSnapshot in snapshot.children) {
                                offerSnapshot.ref.removeValue().also { tasks.add(it) }
                            }
                            Tasks.whenAll(tasks).addOnCompleteListener { offersDeletionTask ->
                                if (offersDeletionTask.isSuccessful) {
                                    // Si toutes les suppressions sont réussies
                                    userList?.remove(user)
                                    usersAdapter?.notifyDataSetChanged()
                                    Toast.makeText(this@ManageUserActivity, "Utilisateur et ses offres supprimés avec succès", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@ManageUserActivity, "La suppression des offres de l'utilisateur a échoué", Toast.LENGTH_SHORT).show()
                                    Log.e("DeleteUser", "La suppression des offres de l'utilisateur a échoué")
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@ManageUserActivity, "Erreur lors de la suppression des offres de l'utilisateur", Toast.LENGTH_SHORT).show()
                            Log.e("DeleteUser", "Erreur lors de la suppression des offres de l'utilisateur : ${error.message}")
                        }
                    })
                } else {
                    Toast.makeText(this, "La suppression de l'utilisateur a échoué", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteUser", "La suppression de l'utilisateur a échoué")
                }
            }
        } else {
            Toast.makeText(this, "L'UID de l'utilisateur est invalide", Toast.LENGTH_SHORT).show()
            Log.e("DeleteUser", "L'UID de l'utilisateur est invalide")
        }
    }*/
    override fun onDeleteUserClick(user: User) {
        if (user.uid.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)
            val offersRef = FirebaseDatabase.getInstance().getReference("offres").orderByChild("providerID").equalTo(user.uid)
            val notifRef = FirebaseDatabase.getInstance().getReference("Notifications").child(user.uid)
            val picLinkRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid).child("profileImage")
            val picStorageRef = FirebaseStorage.getInstance().getReference("profile_images/${user.uid}.jpg")

            // Suppression de l'utilisateur
            userRef.removeValue().addOnCompleteListener { userDeletionTask ->
                if (userDeletionTask.isSuccessful) {
                    // Suppression des offres de l'utilisateur
                    offersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val tasks = mutableListOf<Task<Void>>()
                            val offerImageDeletionTasks = mutableListOf<Task<Void>>()

                            for (offerSnapshot in snapshot.children) {
                                // Récupération des liens des images des offres
                                val offerImageLink = offerSnapshot.child("pictureUrl").getValue(String::class.java)
                                if (offerImageLink != null) {
                                    val offerImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(offerImageLink)
                                    offerImageRef.delete().also { offerImageDeletionTasks.add(it) }
                                }
                                offerSnapshot.ref.removeValue().also { tasks.add(it) }
                            }

                            // Suppression des images des offres dans Firebase Storage
                            Tasks.whenAll(offerImageDeletionTasks).addOnCompleteListener { offerImagesDeletionTask ->
                                if (offerImagesDeletionTask.isSuccessful) {
                                    Tasks.whenAll(tasks).addOnCompleteListener { offersDeletionTask ->
                                        if (offersDeletionTask.isSuccessful) {
                                            // Suppression des notifications de l'utilisateur
                                            notifRef.removeValue().addOnCompleteListener { notifDeletionTask ->
                                                if (notifDeletionTask.isSuccessful) {
                                                    // Suppression de l'image de profil dans Firebase Storage
                                                    picStorageRef.delete().addOnCompleteListener { picStorageDeletionTask ->
                                                        if (picStorageDeletionTask.isSuccessful) {
                                                            // Suppression du lien de l'image de profil dans Firebase Database
                                                            picLinkRef.removeValue().addOnCompleteListener { picLinkDeletionTask ->
                                                                if (picLinkDeletionTask.isSuccessful) {
                                                                    // Si toutes les suppressions sont réussies
                                                                    user.isSupprimer = true
                                                                    userList?.remove(user)
                                                                    usersAdapter?.notifyDataSetChanged()
                                                                    Toast.makeText(this@ManageUserActivity, "Utilisateur, ses offres, images d'offres, notifications et image de profil supprimés avec succès", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(this@ManageUserActivity, "La suppression du lien de l'image de profil de l'utilisateur a échoué", Toast.LENGTH_SHORT).show()
                                                                    Log.e("DeleteUser", "La suppression du lien de l'image de profil de l'utilisateur a échoué")
                                                                }
                                                            }
                                                        } else {
                                                            Toast.makeText(this@ManageUserActivity, "La suppression de l'image de profil de l'utilisateur a échoué", Toast.LENGTH_SHORT).show()
                                                            Log.e("DeleteUser", "La suppression de l'image de profil de l'utilisateur a échoué")
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(this@ManageUserActivity, "La suppression des notifications de l'utilisateur a échoué", Toast.LENGTH_SHORT).show()
                                                    Log.e("DeleteUser", "La suppression des notifications de l'utilisateur a échoué")
                                                }
                                            }
                                        } else {
                                            Toast.makeText(this@ManageUserActivity, "La suppression des offres de l'utilisateur a échoué", Toast.LENGTH_SHORT).show()
                                            Log.e("DeleteUser", "La suppression des offres de l'utilisateur a échoué")
                                        }
                                    }
                                } else {
                                    Toast.makeText(this@ManageUserActivity, "La suppression des images des offres de l'utilisateur a échoué", Toast.LENGTH_SHORT).show()
                                    Log.e("DeleteUser", "La suppression des images des offres de l'utilisateur a échoué")
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@ManageUserActivity, "Erreur lors de la suppression des offres de l'utilisateur", Toast.LENGTH_SHORT).show()
                            Log.e("DeleteUser", "Erreur lors de la suppression des offres de l'utilisateur : ${error.message}")
                        }
                    })
                } else {
                    Toast.makeText(this, "La suppression de l'utilisateur a échoué", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteUser", "La suppression de l'utilisateur a échoué")
                }
            }
        } else {
            Toast.makeText(this, "L'UID de l'utilisateur est invalide", Toast.LENGTH_SHORT).show()
            Log.e("DeleteUser", "L'UID de l'utilisateur est invalide")
        }
    }









}
