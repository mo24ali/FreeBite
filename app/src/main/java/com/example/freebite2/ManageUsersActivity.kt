package com.example.freebite2.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freebite2.R
import com.example.freebite2.adapter.UserListAdapter
import com.example.freebite2.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserListAdapter
    private lateinit var usersList: MutableList<User>

    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        recyclerView = findViewById(R.id.recyclerViewUsers)
            ?: throw NullPointerException("RecyclerView not found in activity_manage_users.xml")

        setupUserRecyclerView()
    }

    private fun setupUserRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        usersList = mutableListOf()
        adapter = UserListAdapter(usersList)
        recyclerView.adapter = adapter

        databaseRef = FirebaseDatabase.getInstance().getReference("Users")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        usersList.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageUsersActivity, "Erreur : ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
