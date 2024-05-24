package com.example.freebite2.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.R
import com.example.freebite2.adapter.OffersAdapter
import com.example.freebite2.databinding.FragmentAccueilBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.activity.AddOffreActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
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
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

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

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Check location permission
        checkLocationPermission()

        // Set up filter icon click listener
        val filterIcon: ImageView = view.findViewById(R.id.filterIcon)
        filterIcon.setOnClickListener {
            showFilterMenu(it)
        }

        // Set up search view listener
        val searchView: SearchView = view.findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Permissions already granted, fetch location
            fetchUserLocation()
        }
    }

    private fun fetchUserLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        userLocation = location
                    } else {
                        Toast.makeText(requireContext(), "Unable to get location. Make sure location is enabled on the device.", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun showFilterMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.filter_menu, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.filter_10km -> {
                    filterOffersByRadius(10)
                    true
                }
                R.id.filter_20km -> {
                    filterOffersByRadius(20)
                    true
                }
                R.id.filter_30km -> {
                    filterOffersByRadius(30)
                    true
                }
                R.id.filter_50km -> {
                    filterOffersByRadius(50)
                    true
                }
                R.id.filter_reset -> {
                    resetFilters()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun filterOffersByRadius(radius: Int) {
        if (userLocation == null) {
            Toast.makeText(requireContext(), "Location not available. Please try again later.", Toast.LENGTH_SHORT).show()
            return
        }

        val filteredList = offreList?.filter { isWithinRadius(it, radius) } ?: listOf()
        offreAdapter?.updateList(filteredList)
    }

    private fun filterOffersByQuery(query: String) {
        val filteredList = offreList?.filter {
            it.nameoffre?.contains(query, ignoreCase = true) == true ||
                    it.details?.contains(query, ignoreCase = true) == true
        } ?: listOf()
        offreAdapter?.updateList(filteredList)
    }

    private fun resetFilters() {
        offreAdapter?.updateList(offreList ?: listOf())
    }

    private fun isWithinRadius(offre: OffreModel, radius: Int): Boolean {
        val offerLocation = Location("").apply {
            latitude = offre.latitude ?: 0.0
            longitude = offre.longitude ?: 0.0
        }

        val distance = userLocation?.distanceTo(offerLocation)?.div(1000) // Convert to kilometers
        return distance != null && distance <= radius
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

    private fun loadOffers() {
        database?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                offreList?.clear()
                for (dataSnapshot in snapshot.children) {
                    val offre = dataSnapshot.getValue(OffreModel::class.java)
                    offre?.let {
                        offreList?.add(it)
                    }
                }
                offreAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", error.message)
                Toast.makeText(requireContext(), "Failed to load offers", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onEditOfferClick(offer: OffreModel) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == offer.providerID) {
            val editFragment = EditOffreFragment()
            val bundle = Bundle()
            bundle.putParcelable("offer", offer)
            editFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fhome, editFragment)
                .addToBackStack(null)
                .commit()
        } else {
            Toast.makeText(requireContext(), "Cela est valable juste pour le propriétaire de poste", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteOfferClick(offer: OffreModel) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        if (offer.offerID == null) {
            Toast.makeText(requireContext(), "L'identifiant de l'offre est invalide", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUserUid == offer.providerID) {
            // Allow deleting the offer
            database?.child(offer.offerID.toString())?.removeValue()
                ?.addOnSuccessListener {
                    Toast.makeText(requireContext(), "Offre supprimée", Toast.LENGTH_SHORT).show()
                }
                ?.addOnFailureListener {
                    Toast.makeText(requireContext(), "Échec de la suppression de l'offre: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Vous n'êtes pas autorisé à supprimer cette offre", Toast.LENGTH_SHORT).show()
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        offreAdapter = null
        offreList = null
        database = null
    }
}
