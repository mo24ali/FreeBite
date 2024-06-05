package com.example.freebite2.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccueilFragment : Fragment(), OffersAdapter.OnOfferClickListener {

    private var _binding: FragmentAccueilBinding? = null
    private lateinit var handler: Handler
    private lateinit var runnableCode: Runnable
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
        handler = Handler(Looper.getMainLooper())
        runnableCode = object : Runnable {
            override fun run() {
                // Refresh your RecyclerView here
                binding.recyclerView.adapter?.let { OffersAdapter.notifyDataSetChanged(it) }
                handler.postDelayed(this, 10000)
            }
        }

        startAutoRefresh()
        _binding = FragmentAccueilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        resetFilters()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        offreList = mutableListOf()
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        checkLocationPermission()

        val filterIcon: ImageView = view.findViewById(R.id.filterIcon)
        filterIcon.setOnClickListener {
            showFilterMenu(it)
        }

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
            fetchUserLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchUserLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    userLocation = location
                } else {
                    requestNewLocationData()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                userLocation = location
                fusedLocationClient.removeLocationUpdates(this)
            }
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
                R.id.filter_05km -> {
                    filterOffersByRadius(5)
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
        val offreDetailsFragment = OffreDetailsFragment()

        val bundle = Bundle()
        bundle.putParcelable("offre", offer)
        offreDetailsFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fhome, offreDetailsFragment)
            .addToBackStack(null)
            .commit()
        Toast.makeText(requireContext(), "Clicked on ${offer.nameoffre}", Toast.LENGTH_SHORT).show()
    }

    private fun startAutoRefresh() {
        handler.postDelayed(runnableCode, 10000)
    }

    private fun stopAutoRefresh() {
        handler.removeCallbacks(runnableCode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoRefresh()
        _binding = null
        offreAdapter = null
        offreList = null
        database = null
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
