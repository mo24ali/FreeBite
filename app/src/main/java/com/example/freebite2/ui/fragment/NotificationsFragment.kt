package com.example.freebite2.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.adapter.NotificationsAdapter
import com.example.freebite2.databinding.FragmentNotificationsBinding
import com.example.freebite2.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var database: DatabaseReference
    private lateinit var notificationsAdapter: NotificationsAdapter
    private var notificationList: MutableList<Notification> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().getReference("Notifications")
        notificationsAdapter = NotificationsAdapter(notificationList, database, requireContext())
        setupRecyclerView()
        fetchNotifications()
    }

    private fun setupRecyclerView() {
        binding.recyclerView2.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView2.adapter = notificationsAdapter
    }

    /*private fun fetchNotifications() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notificationList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val notification = dataSnapshot.getValue(Notification::class.java)
                        if (notification != null) {
                            // Assigner l'ID de la notification à la clé du dataSnapshot
                            notification.id = dataSnapshot.key.toString()
                            notificationList.add(notification)
                        }
                    }
                    Log.d("NotificationsFragment", "Récupéré ${notificationList.size} notifications")
                    notificationsAdapter.notifyDataSetChanged() // Forcer la mise à jour de l'UI
                    toggleEmptyView()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationsFragment", "Erreur de la base de données : ${error.message}")
                }
            })
        }
    }*/
    private fun fetchNotifications() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            database.child(currentUserId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notificationList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val notification = dataSnapshot.getValue(Notification::class.java)
                        if (notification != null) {
                            // Assigner l'ID de la notification à la clé du dataSnapshot
                            notification.id = dataSnapshot.key.toString()
                            notificationList.add(notification)
                        }
                    }
                    Log.d("NotificationsFragment", "Récupéré ${notificationList.size} notifications")
                    notificationsAdapter.notifyDataSetChanged() // Forcer la mise à jour de l'UI
                    toggleEmptyView()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationsFragment", "Erreur de la base de données : ${error.message}")
                }
            })
        }
    }

    private fun toggleEmptyView() {
        if (notificationList.isEmpty()) {
            binding.emptyView1.visibility = View.VISIBLE
            binding.recyclerView2.visibility = View.GONE
        } else {
            binding.emptyView1.visibility = View.GONE
            binding.recyclerView2.visibility = View.VISIBLE
        }
    }
}
