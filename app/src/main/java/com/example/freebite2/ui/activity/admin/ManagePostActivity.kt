package com.example.freebite2.ui.activity.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.adapter.OffersAdapterAdmin
import com.example.freebite2.databinding.ActivityManagePostBinding
import com.example.freebite2.model.OffreModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManagePostActivity : AppCompatActivity(), OffersAdapterAdmin.OnOfferClickListener {

    private lateinit var binding: ActivityManagePostBinding
    private var offreAdapter: OffersAdapterAdmin? = null
    private var offreList: MutableList<OffreModel>? = null
    private var database: DatabaseReference? = null
    private var auth: FirebaseAuth? = null
    private val adminUid = "tZtTbchwFPOse0jhTXt39kCnNGo1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        offreList = mutableListOf()
        auth = FirebaseAuth.getInstance()

        offreAdapter = OffersAdapterAdmin(offreList ?: mutableListOf(), this)
        binding.recyclerViewPostsAdmin.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPostsAdmin.adapter = offreAdapter
        binding.backBtn3Admin.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
        val currentUser = auth?.currentUser

        if (currentUser != null) {
            if (currentUser.uid != adminUid) {
                Toast.makeText(this, "Vous n'êtes pas autorisé à modifier ce post", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            database = FirebaseDatabase.getInstance().getReference("offres")
            database?.addValueEventListener(object : ValueEventListener {
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

    /*override fun onOfferClick(offer: OffreModel) {
        toggleVisibility(View.GONE)
        val offreDetailsFragment = OffreDetailsFragment()

        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        offreDetailsFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.hooldeAdmin, offreDetailsFragment)
            .addToBackStack(null)
            .commit()
    }*/
    override fun onOfferClick(offer: OffreModel) {
        toggleVisibility(View.GONE)

        val intent = Intent(this, AccessPostActivity::class.java)
        intent.putExtra("offre", offer)
        startActivity(intent)
    }


    /*override fun onEditOfferClick(offer: OffreModel) {
        toggleVisibility(View.GONE)
        val editDeletePostFragment = ManageEditDeletePostFragment()
        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        editDeletePostFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.hooldeAdmin, editDeletePostFragment)
            .addToBackStack(null)
            .commit()
    }*/
    override fun onEditOfferClick(offer: OffreModel) {
        toggleVisibility(View.GONE)
        val intent = Intent(this,EditPostActivity::class.java)
        intent.putExtra("offre", offer)
        startActivity(intent)
    }


    private fun toggleVisibility(visibility: Int) {
        binding.recyclerViewPostsAdmin.visibility = visibility
        binding.backBtn3Admin.visibility = visibility
        binding.hooldeAdmin.visibility = if (visibility == View.GONE) View.VISIBLE else View.GONE
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
            Log.e("ManagePostActivity", "L'ID de l'offre est null")
            return
        }
        database?.child(offer.offerID!!)?.removeValue()
    }
}
