package com.example.freebite2.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.R
import com.example.freebite2.adapter.OffersAdapterUser
import com.example.freebite2.databinding.ActivityUserOffersBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.fragment.OffreDetailsFragment
import com.example.freebite2.ui.fragment.UpdateOffreFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserOffersActivity : AppCompatActivity(), OffersAdapterUser.OnOfferClickListener {

    private lateinit var binding: ActivityUserOffersBinding
    private var offreAdapter: OffersAdapterUser? = null
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
        offreAdapter = OffersAdapterUser(offreList ?: mutableListOf(), this)
        binding.recyclerViewUserOffers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUserOffers.adapter = offreAdapter
        binding.backBtn3.setOnClickListener {
            startActivity(Intent(this, MainHomeActivity::class.java))
        }
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
        val offreDetailsFragment = OffreDetailsFragment()

        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        offreDetailsFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fhomeUpdateOffer, offreDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onEditOfferClick(offer: OffreModel) {
        val fragment = UpdateOffreFragment()
        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fhomeUpdateOffer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDeleteOfferClick(offer: OffreModel) {
        if (offer.offerID == null) {
            Log.e("UserOffersActivity", "L'ID de l'offre est null")
            return
        }
        database?.child(offer.offerID!!)?.removeValue()
    }
}
