package com.example.freebite2.ui.activity.admin

import android.app.AlertDialog
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
import com.example.freebite2.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

    /*private fun fetchUsersFromDatabase() {


     */
     /*
        database = FirebaseDatabase.getInstance().getReference("Users")
        database?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList?.clear()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        userList?.add(user)
                    }
                }
                usersAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", error.message)
            }
        })
    }*/
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
        // Gérer le clic sur l'utilisateur
        showUserInfoDialog(user)
    }

    private fun showUserInfoDialog(user: User) {
        val dialogBinding = DialogUserInfoBinding.inflate(LayoutInflater.from(this))

        // Vérifiez et définissez le nom de l'utilisateur
        val userName = "${user.nom ?: ""} ${user.prenom ?: ""}".trim()
        dialogBinding.userName.text = if (userName.isNotEmpty()) userName else "Nom inconnu"

        // Vérifiez et définissez l'email de l'utilisateur
        dialogBinding.userEmail.text = user.email ?: "Email inconnu"

        // Charger l'image de profil de l'utilisateur avec Glide
        val profileUrl = user.profilePictureUrl ?: ""
        Glide.with(this)
            .load(profileUrl)
            .placeholder(R.drawable.profile_holder_user) // Placeholder en cas de chargement
            .error(R.drawable.profile_holder_user) // Image de substitution en cas d'erreur
            .fallback(R.drawable.profile_holder_user) // Image de substitution par défaut
            .into(dialogBinding.userImage)

        AlertDialog.Builder(this)
            .setTitle("Informations de l'utilisateur")
            .setView(dialogBinding.root)
            .setPositiveButton("OK", null)
            .show()
    }



    override fun onEditUserClick(user: User) {
        // Gérer la modification de l'utilisateur
    }

    override fun onDeleteUserClick(user: User) {
        if (user.uid != null && user.uid.isNotEmpty()) {
            // Utiliser le chemin correct pour accéder à l'utilisateur spécifique
            val userRef = database?.child(user.uid)

            userRef?.removeValue()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userList?.remove(user)
                    usersAdapter?.notifyDataSetChanged()
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
