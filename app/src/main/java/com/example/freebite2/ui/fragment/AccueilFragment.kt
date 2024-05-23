package com.example.freebite2.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.R
import com.example.freebite2.adapter.OffersAdapter
import com.example.freebite2.databinding.FragmentAccueilBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.activity.AddOffreActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccueilFragment : Fragment(), OffersAdapter.OnOfferClickListener {

    private var _binding: FragmentAccueilBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is not initialized")
    private var offreAdapter: OffersAdapter? = null
    private var offreList: MutableList<OffreModel>? = null
    private var database: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccueilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        offreList = mutableListOf()

        // Pass the fragment instance as the OnOfferClickListener
        offreAdapter = OffersAdapter(offreList ?: mutableListOf(), this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = offreAdapter

        binding.addOffreButton.setOnClickListener {
            val addIntent = Intent(activity, AddOffreActivity::class.java)
            startActivity(addIntent)
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
                Log.e("DatabaseError", error.message)
            }
        })
    }

    override fun onOfferClick(offer: OffreModel) {
        // Handle item click here, for example:
        val offreDetailsFragment = OffreDetailsFragment()

        // Pass the OffreModel object as an argument using a Bundle
        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        offreDetailsFragment.arguments = bundle

        // Use the FragmentManager to begin a FragmentTransaction, replace the current fragment with OffreDetailsFragment, and commit the transaction
        parentFragmentManager.beginTransaction()
            .replace(R.id.fhome, offreDetailsFragment) // Replace 'container' with the id of your FrameLayout or the container for your fragments
            .addToBackStack(null)
            .commit()
        Toast.makeText(requireContext(), "Clicked on ${offer.nameoffre}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        offreAdapter = null
        offreList = null
        database = null
    }
}