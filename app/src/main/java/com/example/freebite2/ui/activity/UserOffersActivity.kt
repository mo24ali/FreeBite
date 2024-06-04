package com.example.freebite2.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.R
import com.example.freebite2.adapter.OffersAdapterUser
import com.example.freebite2.databinding.ActivityUserOffersBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.fragment.OffreDetailsFragment
import com.example.freebite2.ui.fragment.UpdateOffreFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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
        toggleVisibility(View.GONE)
        val offreDetailsFragment = OffreDetailsFragment()

        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        offreDetailsFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.hoolde, offreDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onEditOfferClick(offer: OffreModel) {
        toggleVisibility(View.GONE)
        val updateOffreFragment = UpdateOffreFragment()
        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        updateOffreFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.hoolde, updateOffreFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun toggleVisibility(visibility: Int) {
        binding.recyclerViewUserOffers.visibility = visibility
        binding.backBtn3.visibility = visibility
        binding.hoolde.visibility = if (visibility == View.GONE) View.VISIBLE else View.GONE
    }
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            toggleVisibility(View.VISIBLE)
            super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }
    override fun onResume() {
        super.onResume()
        // Ensure the RecyclerView and other views are visible
        toggleVisibility(View.VISIBLE)
    }

    override fun onDeleteOfferClick(offer: OffreModel) {
        if (offer.offerID == null) {
            Log.e("UserOffersActivity", "L'ID de l'offre est null")
            return
        }
        database?.child(offer.offerID!!)?.removeValue()
    }
}

/*package com.example.freebite2.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.R
import com.example.freebite2.adapter.OffersAdapterUser
import com.example.freebite2.databinding.ActivityUserOffersBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.fragment.OffreDetailsFragment
import com.example.freebite2.ui.fragment.UpdateOffreFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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
        toggleVisibility(View.GONE)
        val offreDetailsFragment = OffreDetailsFragment()

        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        offreDetailsFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.hoolde, offreDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onEditOfferClick(offer: OffreModel) {
        toggleVisibility(View.GONE)
        val updateOffreFragment = UpdateOffreFragment().apply {
            arguments = Bundle().apply {
                putParcelable("offre", offer)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.hoolde, updateOffreFragment)
            .addToBackStack(null)
            .commit()
    }


    override fun onResume() {
        super.onResume()
        // Ensure the RecyclerView and other views are visible
        toggleVisibility(View.VISIBLE)
    }
    private fun toggleVisibility(visibility: Int) {
        binding.recyclerViewUserOffers.visibility = visibility
        binding.hoolde.visibility = if (visibility == View.GONE) View.VISIBLE else View.GONE
    }
    override fun onDeleteOfferClick(offer: OffreModel) {
        if (offer.offerID == null) {
            Log.e("UserOffersActivity", "L'ID de l'offre est null")
            return
        }
        database?.child(offer.offerID!!)?.removeValue()
    }
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            toggleVisibility(View.VISIBLE)
        } else {
            super.onBackPressed()
        }
    }
}
*/