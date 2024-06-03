package com.example.freebite2.ui.activity.admin

import UserAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.databinding.ActivityAdminBinding
import com.example.freebite2.model.User
import com.example.freebite2.ui.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userAdapter: UserAdapter


    private var userList: MutableList<User> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

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


        binding.managePost.setOnClickListener {
            startActivity(Intent(this, ManagePostActivity::class.java))

        }
        binding.manageUser.setOnClickListener {

            startActivity(Intent(this, ManageUserActivity::class.java))

        }
        binding.manageNotification.setOnClickListener {

            startActivity(Intent(this, ManageNotificationActivity::class.java))
        }
        binding.manageRaport.setOnClickListener {

            startActivity(Intent(this, ManageRaportActivity::class.java))
        }

        // Handle Manage User Accounts button click
      //  binding.manageUserAccountsCard.setOnClickListener {
     //       startActivity(Intent(this, ManageUsersActivity::class.java))
    //    }

        // Setup RecyclerView for users
       // setupUserRecyclerView()
    }

 /*   private fun setupUserRecyclerView() {
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
    }   */
}
