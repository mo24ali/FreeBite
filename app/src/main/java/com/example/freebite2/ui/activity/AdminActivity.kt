package com.example.freebite2.ui.activity

import UserAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freebite2.R
import com.example.freebite2.databinding.ActivityAdminBinding
import com.example.freebite2.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userAdapter: UserAdapter
    private lateinit var recyclerView: RecyclerView

    private var userList: MutableList<User> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Assurez-vous que R.id.recyclerViewUserList est bien défini dans activity_admin.xml
        recyclerView = findViewById(R.id.recyclerViewUsers) ?: throw NullPointerException("RecyclerView not found")

        setupUserRecyclerView()
        // Handle logout
        binding.logOutB.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Handle back button
        binding.backB.setOnClickListener {
            finish()
        }

        // Handle View Offers button click
        binding.viewDonorRequestsCard.setOnClickListener {

            viewOffers()
        }

        // Handle Manage User Accounts button click
        binding.manageUserAccountsCard.setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        // Setup RecyclerView for users
        setupUserRecyclerView()
    }

    private fun setupUserRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch user accounts from Firebase
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }
                userAdapter = UserAdapter(userList, this@AdminActivity)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(this@AdminActivity, "Failed to fetch user accounts", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun viewOffers() {
        // Handle viewing offers
    }
}
