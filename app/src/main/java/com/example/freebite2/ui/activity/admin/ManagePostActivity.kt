package com.example.freebite2.ui.activity.admin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.adapter.OffersAdapter
import com.example.freebite2.databinding.ActivityManagePostBinding
import com.example.freebite2.model.OffreModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManagePostActivity : AppCompatActivity(), OffersAdapter.OnOfferClickListener {

    private lateinit var binding: ActivityManagePostBinding
    private lateinit var offreAdapter: OffersAdapter
    private lateinit var offreList: MutableList<OffreModel>
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()
        fetchOffersFromDatabase()
    }

    private fun setupRecyclerView() {
        offreList = mutableListOf()
        offreAdapter = OffersAdapter(offreList, this)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = offreAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    filterOffersByQuery(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filterOffersByQuery(newText)
                }
                return false
            }
        })
    }

    private fun fetchOffersFromDatabase() {
        database = FirebaseDatabase.getInstance().getReference("offres")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                offreList.clear()
                for (dataSnapshot in snapshot.children) {
                    val offre = dataSnapshot.getValue(OffreModel::class.java)
                    if (offre != null) {
                        offreList.add(offre)
                    }
                }
                offreAdapter.updateList(offreList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", error.message)
            }
        })
    }

    private fun filterOffersByQuery(query: String) {
        val filteredList = offreList.filter {
            it.nameoffre?.contains(query, ignoreCase = true) == true ||
                    it.details?.contains(query, ignoreCase = true) == true
        }
        offreAdapter.updateList(filteredList)
    }

    override fun onOfferClick(offer: OffreModel) {
        // Handle offer click
        Toast.makeText(this, "Clicked on ${offer.nameoffre}", Toast.LENGTH_SHORT).show()
    }

     fun onEditOfferClick(offer: OffreModel) {
        // Handle edit offer click
        Toast.makeText(this, "Edit ${offer.nameoffre}", Toast.LENGTH_SHORT).show()
    }

     fun onDeleteOfferClick(offer: OffreModel) {
        // Handle delete offer click
        Toast.makeText(this, "Delete ${offer.nameoffre}", Toast.LENGTH_SHORT).show()
    }
}
