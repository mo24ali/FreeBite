package com.example.freebite2.ui.activity.admin


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.databinding.ActivityAdminBinding
import com.example.freebite2.ui.activity.MainActivity
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        // Add ValueEventListener for the "users" node
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the count of children under the "users" node
                val numberOfUsers = snapshot.childrenCount.toInt()
                // Set the text for nbUser TextView
                binding.nbUser.text = numberOfUsers.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if any
            }
        })

        // Add ValueEventListener for the "offers" node
        val offersRef = FirebaseDatabase.getInstance().getReference("offres")
        offersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the count of children under the "offers" node
                val numberOfOffers = snapshot.childrenCount.toInt()
                // Set the text for nbOffre TextView
                binding.nbOffre.text = numberOfOffers.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if any
            }
        })
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
        /*binding.manageNotification.setOnClickListener {

            startActivity(Intent(this, ManageNotificationActivity::class.java))
        }*/
       /* binding.manageRaport.setOnClickListener {

            startActivity(Intent(this, ManageRaportActivity::class.java))
        }*/


    }
}