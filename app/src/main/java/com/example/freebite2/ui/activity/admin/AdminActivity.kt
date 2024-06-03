package com.example.freebite2.ui.activity.admin


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.databinding.ActivityAdminBinding
import com.example.freebite2.ui.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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


    }
}