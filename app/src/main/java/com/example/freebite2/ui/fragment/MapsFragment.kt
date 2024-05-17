package com.example.freebite2.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.freebite2.R
import com.example.freebite2.model.OffreModel
import kotlin.math.*

class MapsFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val radius = 10.0 // Radius in km

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        initializeLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fmap) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun initializeLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentUserLocation = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15f))
                fetchNearbyOffers(currentUserLocation)
            }
        }
    }

    private fun fetchNearbyOffers(currentUserLocation: LatLng) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").get().addOnSuccessListener { result ->
            val nearbyOffers = mutableListOf<OffreModel>()
            for (document in result) {
                val userID = document.id
                val userLocation = document.getGeoPoint("location")
                if (userLocation != null) {
                    val distance = calculateDistance(currentUserLocation, LatLng(userLocation.latitude, userLocation.longitude))
                    if (distance <= radius) {
                        val offers = document.get("offers") as? Map<String, String> ?: continue
                        for ((offerID, details) in offers) {
                            nearbyOffers.add(OffreModel(userID, offerID, details, distance, userLocation.latitude, userLocation.longitude))
                        }
                    }
                }
            }
            displayNearbyOffers(nearbyOffers)
        }
    }

    private fun calculateDistance(loc1: LatLng, loc2: LatLng): Double {
        val earthRadius = 6371.0 // Radius of the earth in km
        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(loc1.latitude)) * cos(Math.toRadians(loc2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun displayNearbyOffers(offers: List<OffreModel>) {
        for (offer in offers) {
            val markerOptions = MarkerOptions()
                .position(LatLng(offer.latitude!!, offer.longitude!!))
                .title(offer.details)
            googleMap.addMarker(markerOptions)
        }
    }
}
