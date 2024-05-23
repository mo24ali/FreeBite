package com.example.freebite2.ui.fragment

/*
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.freebite2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import kotlin.math.*

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private val radius = 10.0 // Radius in km

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fmap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Set up the manual location update button
        val btnUpdateLocation = view.findViewById<Button>(R.id.btnUpdateLocation)
        btnUpdateLocation.setOnClickListener {
            updateLocationManually()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    private fun updateLocationManually() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.clear() // Clear the map before adding new markers
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                fetchNearbyOffers(currentLocation)
            }
        }
    }

    private fun fetchNearbyOffers(currentUserLocation: LatLng) {
        val maxDistance = 50000 // 50 kilometers
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val locationSnapshot = userSnapshot.child("location")
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java)
                  //  val profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java)
                    val username = userSnapshot.child("nom").getValue(String::class.java)
                    if (latitude != null && longitude != null ) {    //&& profileImageUrl != null
                        val userLocation = LatLng(latitude, longitude)
                        val distance = calculateDistance(
                            currentUserLocation.latitude, currentUserLocation.longitude,
                            userLocation.latitude, userLocation.longitude
                        )

                        if (distance <= maxDistance) {
                            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.map_marker).scale(90,90)
                            val markerOptions = MarkerOptions()
                                .position(userLocation)
                                .title(/* title = */ username)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))

                            googleMap.addMarker(markerOptions)
                            // Load the profile image and use it as a marker icon
                           // loadProfileImageAndAddMarker(profileImageUrl, userLocation)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun loadProfileImageAndAddMarker(profileImageUrl: String, userLocation: LatLng) {
        Glide.with(this)
            .asBitmap()
            .load(profileImageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title("Nearby User")
                        .icon(BitmapDescriptorFactory.fromBitmap(resource))

                    googleMap.addMarker(markerOptions)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle if needed
                }
            })
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c * 1000 // Convert to meters
    }
}



*/

import android.Manifest
import android.content.pm.PackageManager
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.freebite2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import kotlin.math.*

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private var radius = 50 // Default radius in km

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fmap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Set up the manual location update button
        // val btnUpdateLocation = view.findViewById<Button>(R.id.btnUpdateLocation)
        //    btnUpdateLocation.setOnClickListener {
        //       googleMap.clear()
        //updateLocationManually()
        //     }

        // Set up the choose type button
        val btnChooseType = view.findViewById<Button>(R.id.btnChooseType)
        btnChooseType.setOnClickListener {
            showChooseTypeDialog()
        }

        // Set up the seek bar
        val seekBarDistance = view.findViewById<SeekBar>(R.id.seekBarDistance)
        val tvDistanceLabel = view.findViewById<TextView>(R.id.tvDistanceLabel)
        seekBarDistance.setOnSeekBarChangeListener(object : SeekBar
            .OnSeekBarChangeListener {
             override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radius = progress
                tvDistanceLabel.text = "Radius (km): $progress"
             }

             override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
             }

             override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
             }
        } )







    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    private fun updateLocationManually() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.clear() // Clear the map before adding new markers
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                fetchNearbyUsers(currentLocation)
            }
        }
    }

    private fun showChooseTypeDialog() {
        val options = arrayOf("Nearby Users", "Nearby Offers")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Information Type")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> updateLocationManually()
                    1 -> updateOffersManually()
                }
            }
            .show()
    }

    private fun updateOffersManually() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.clear() // Clear the map before adding new markers
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                fetchNearbyOffers(currentLocation)
            }
        }
    }

    private fun fetchNearbyOffers(currentUserLocation: LatLng) {
        val maxDistance = radius * 1000 // Convert km to meters
        database.child("offres").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (offreSnapshot in snapshot.children) {
                    //  val locationSnapshot = userSnapshot.child("location")
                    val latitude = offreSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = offreSnapshot.child("longitude").getValue(Double::class.java)
                    val pictureUrl = offreSnapshot.child("pictureUrl").getValue(String::class.java)
                    val offrename = offreSnapshot.child("pictureUrl").getValue(String::class.java)
                    if (latitude != null && longitude != null && pictureUrl != null && offrename != null) {
                        val offreLocation = LatLng(latitude, longitude)

                        val distance = calculateDistance(
                            currentUserLocation.latitude, currentUserLocation.longitude,
                            offreLocation.latitude, offreLocation.longitude
                        )

                        if (distance <= maxDistance) {
                            // Load the profile image and use it as a marker icon
                            loadProfileImageAndAddMarker(pictureUrl, offreLocation,offrename)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }


    private fun fetchNearbyUsers(currentUserLocation: LatLng) {
        val maxDistance = radius * 1000 // Convert km to meters
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val locationSnapshot = userSnapshot.child("location")
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java)
                    //  val profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java)
                    val username = userSnapshot.child("nom").getValue(String::class.java)
                    if (latitude != null && longitude != null ) {    //&& profileImageUrl != null
                        val userLocation = LatLng(latitude, longitude)
                        val distance = calculateDistance(
                            currentUserLocation.latitude, currentUserLocation.longitude,
                            userLocation.latitude, userLocation.longitude
                        )

                        if (distance <= maxDistance) {
                            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.map_marker).scale(90,90)
                            val markerOptions = MarkerOptions()
                                .position(userLocation)
                                .title(/* title = */ username)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))

                            googleMap.addMarker(markerOptions)
                            // Load the profile image and use it as a marker icon
                            // loadProfileImageAndAddMarker(profileImageUrl, userLocation)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }


    private fun loadProfileImageAndAddMarker(profileImageUrl: String, userLocation: LatLng,offrename: String) {
        Glide.with(this)
            .asBitmap()
            .load(profileImageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title(offrename)
                        .icon(BitmapDescriptorFactory.fromBitmap(resource.scale(100,100)))

                    googleMap.addMarker(markerOptions)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle if needed
                }
            })
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c * 1000 // Convert to meters
    }
}












////////////////////////////////////////////////

/*
class MapsFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val radius = 20.0 // Radius in km
    private lateinit var database: DatabaseReference

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        initializeLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        database = FirebaseDatabase.getInstance().getReference("users")
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

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update location every 10 seconds
            fastestInterval = 5000 // Update location every 5 seconds in the fastest case
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations){
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                        googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                        fetchNearbyOffers(currentLocation)
                    }
                }
            }
        }, Looper.getMainLooper())
    }

    private fun fetchNearbyOffers(currentUserLocation: LatLng) {

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nearbyOffers = mutableListOf<OffreModel>()
                for (userSnapshot in snapshot.children) {
                    val userID = userSnapshot.key ?: continue
                    val userLocation = userSnapshot.child("location").getValue(Location::class.java)
                    if (userLocation != null) {
                        val distance = calculateDistance(currentUserLocation, LatLng(userLocation.latitude, userLocation.longitude))
                        if (distance <= radius) {
                            val offersSnapshot = userSnapshot.child("offers")
                            for (offerSnapshot in offersSnapshot.children) {
                                val offerID = offerSnapshot.key ?: continue
                                val details = offerSnapshot.getValue(String::class.java) ?: continue
                                nearbyOffers.add(OffreModel(userID, offerID, nameoffre = null, details, duration = null, userLocation.latitude, userLocation.longitude))
                            }
                        }
                    }
                }
                displayNearbyOffers(nearbyOffers)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
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
*/

///////////////*
/*

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.freebite2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private val radius = 10.0 // Radius in km

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fmap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        initializeLocation()
    }

    private fun initializeLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update location every 10 seconds
            fastestInterval = 5000 // Update location every 5 seconds in the fastest case
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                        googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                        fetchNearbyOffers(currentLocation)
                    }
                }
            }
        }, Looper.getMainLooper())
    }

    private fun fetchNearbyOffers(currentUserLocation: LatLng) {
        val maxDistance = 50000 // 50 kilometers
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val locationSnapshot = userSnapshot.child("location")
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java)

                    if (latitude != null && longitude != null) {
                        val userLocation = LatLng(latitude, longitude)
                        val distance = calculateDistance(
                            currentUserLocation.latitude, currentUserLocation.longitude,
                            userLocation.latitude, userLocation.longitude
                        )

                        if (distance <= maxDistance) {
                            val markerOptions = MarkerOptions()
                                .position(userLocation)
                                .title("Nearby User")
                                // Use a custom icon

                            googleMap.addMarker(markerOptions)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c * 1000 // Convert to meters
    }
}


*/

// class MapsFragment : Fragment() {
//
// private lateinit var fusedLocationClient: FusedLocationProviderClient
// private lateinit var googleMap: GoogleMap
// private val radius = 10.0 // Radius in km
//
// private val callback = OnMapReadyCallback { map ->
// googleMap = map
// initializeLocation()
// }
//
// override fun onCreateView(
// inflater: LayoutInflater, container: ViewGroup?,
// savedInstanceState: Bundle?
// ): View? {
// return inflater.inflate(R.layout.fragment_maps, container, false)
// }
//
// override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
// super.onViewCreated(view, savedInstanceState)
// val mapFragment = childFragmentManager.findFragmentById(R.id.fmap) as SupportMapFragment?
// mapFragment?.getMapAsync(callback)
// }
//
// private fun initializeLocation() {
// fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//
// if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
// ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
// ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
// return
// }
//
// val locationRequest = LocationRequest.create().apply {
// interval = 10000 // Update location every 10 seconds
// fastestInterval = 5000 // Update location every 5 seconds in the fastest case
// priority = LocationRequest.PRIORITY_HIGH_ACCURACY
// }
//
// fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
// override fun onLocationResult(locationResult: LocationResult) {
// for (location in locationResult.locations) {
// if (location != null) {
// val currentLocation = LatLng(location.latitude, location.longitude)
// googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
// googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
// fetchNearbyOffers(currentLocation)
// }
// }
// }
// }, Looper.getMainLooper())
// }
//
// private fun fetchNearbyOffers(currentUserLocation: LatLng) {
// val db = FirebaseDatabase.getInstance().reference.child("Users")
// db.addListenerForSingleValueEvent(object : ValueEventListener {
// override fun onDataChange(dataSnapshot: DataSnapshot) {
// val nearbyOffers = mutableListOf<OffreModel>()
// for (snapshot in dataSnapshot.children) {
// val userID = snapshot.key
// val userLocation = snapshot.child("location").getValue(Location::class.java)
// if (userLocation != null) {
// val distance = calculateDistance(currentUserLocation, LatLng(userLocation.latitude, userLocation.longitude))
// if (distance <= radius) {
// val offers = snapshot.child("offers").children.mapNotNull { offerSnapshot ->
// val offerID = offerSnapshot.key
// val details = offerSnapshot.getValue(String::class.java)
// if (offerID != null && details != null) {
// OffreModel(userID!!, offerID, details, null, userLocation.latitude, userLocation.longitude)
// } else {
// null
// }
// }
// nearbyOffers.addAll(offers)
// }
// }
// }
// displayNearbyOffers(nearbyOffers)
// }
//
// override fun onCancelled(databaseError: DatabaseError) {
// // Handle possible errors.
// }
// })
// }
//
// private fun calculateDistance(loc1: LatLng, loc2: LatLng): Double {
// val earthRadius = 6371.0 // Radius of the earth in km
// val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
// val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
// val a = sin(dLat / 2) * sin(dLat / 2) +
// cos(Math.toRadians(loc1.latitude)) * cos(Math.toRadians(loc2.latitude)) *
// sin(dLon / 2) * sin(dLon / 2)
// val c = 2 * atan2(sqrt(a), sqrt(1 - a))
// return earthRadius * c
// }
//
// private fun displayNearbyOffers(offers: List<OffreModel>) {
// for (offer in offers) {
// val markerOptions = MarkerOptions()
// .position(LatLng(offer.latitude!!, offer.longitude!!))
// .title(offer.details)
// googleMap.addMarker(markerOptions)
// }
//  }
// }