package com.example.freebite2.ui.activity.admin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.adapter.UsersAdapter
import com.example.freebite2.databinding.ActivityManageUserBinding
import com.example.freebite2.model.User
import com.google.firebase.database.*

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
        // Handle user click
    }

    override fun onEditUserClick(user: User) {
        // Handle edit user
    }

    override fun onDeleteUserClick(user: User) {
        // Handle delete user
    }
}
