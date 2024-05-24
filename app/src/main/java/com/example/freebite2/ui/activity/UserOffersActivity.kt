/*
package com.example.freebite2.ui.activity


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.R
import com.example.freebite2.adapter.OffersAdapter
import com.example.freebite2.databinding.ActivityUserOffersBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.fragment.OffreDetailsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserOffersActivity : AppCompatActivity(), OffersAdapter.OnOfferClickListener {

    private lateinit var binding: ActivityUserOffersBinding
    private var offreAdapter: OffersAdapter? = null
    private var offreList: MutableList<OffreModel>? = null
    private var database: DatabaseReference? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserOffersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        offreList = mutableListOf()
        auth = FirebaseAuth.getInstance()

        // Pass the activity instance as the OnOfferClickListener
        offreAdapter = OffersAdapter(offreList ?: mutableListOf(), this)
        binding.recyclerViewUserOffers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUserOffers.adapter = offreAdapter

        val currentUser = auth?.currentUser

        if (currentUser != null) {
            database = FirebaseDatabase.getInstance().getReference("offres")
            database?.orderByChild("providerID")?.equalTo(currentUser.uid)
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        offreList?.clear()
                        for (dataSnapshot in snapshot.children) {
                            val offre = dataSnapshot.getValue(OffreModel::class.java)
                            if (offre != null) {
                                offreList?.add(offre)
                            }
                        }
                        offreAdapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                    }
                })
        }
    }

    override fun onOfferClick(offer: OffreModel) {
        // Handle item click here, for example:
        val offreDetailsFragment = OffreDetailsFragment()

        // Pass the OffreModel object as an argument using a Bundle
        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        offreDetailsFragment.arguments = bundle

        // Use the FragmentManager to begin a FragmentTransaction, replace the current fragment with OffreDetailsFragment, and commit the transaction
        supportFragmentManager.beginTransaction()
            .replace(R.id.fhome, offreDetailsFragment)
            .addToBackStack(null)
            .commit()
    }
}


 */
package com.example.freebite2.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.adapter.OffersAdapter
import com.example.freebite2.databinding.ActivityUserOffersBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.fragment.OffreDetailsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserOffersActivity : AppCompatActivity(), OffersAdapter.OnOfferClickListener {

    private lateinit var binding: ActivityUserOffersBinding
    private var offreAdapter: OffersAdapter? = null
    private var offreList: MutableList<OffreModel>? = null
    private var database: DatabaseReference? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserOffersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        offreList = mutableListOf()
        auth = FirebaseAuth.getInstance()

        // Pass the activity instance as the OnOfferClickListener
        offreAdapter = OffersAdapter(offreList ?: mutableListOf(), this)
        binding.recyclerViewUserOffers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUserOffers.adapter = offreAdapter

        val currentUser = auth?.currentUser

        if (currentUser != null) {
            database = FirebaseDatabase.getInstance().getReference("offres")
            database?.orderByChild("providerID")?.equalTo(currentUser.uid)
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        offreList?.clear()
                        for (dataSnapshot in snapshot.children) {
                            val offre = dataSnapshot.getValue(OffreModel::class.java)
                            if (offre != null) {
                                offreList?.add(offre)
                            }
                        }
                        offreAdapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                    }
                })
        }
    }

    override fun onOfferClick(offer: OffreModel) {
        // Handle item click here, for example:
        val offreDetailsFragment = OffreDetailsFragment()

        // Pass the OffreModel object as an argument using a Bundle
        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        offreDetailsFragment.arguments = bundle

        // Use the FragmentManager to begin a FragmentTransaction, replace the current fragment with OffreDetailsFragment, and commit the transaction
       /*supportFragmentManager.beginTransaction()
            .replace(R.id.fhome, offreDetailsFragment)
            .addToBackStack(null)
            .commit()*/
    }

    override fun onEditOfferClick(offer: OffreModel) {
        // Navigate to an edit screen
  /*      val editOfferIntent = Intent(this, EditOfferActivity::class.java)
        editOfferIntent.putExtra("offer", offer)
        startActivity(editOfferIntent)*/
    }

    override fun onDeleteOfferClick(offer: OffreModel) {
        // Remove offer from the database
        database?.child(offer.offerID!!)?.removeValue()
    }
}
