package com.example.freebite2.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.freebite2.R
import com.example.freebite2.databinding.InfoCardBinding
import com.example.freebite2.model.OffreModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private var radius = 50 // Default radius in km
    private lateinit var binding: InfoCardBinding

    private fun checkGPSStatus() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledDialog()
        }
    }

    private fun showGPSDisabledDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Le GPS est désactivé. Voulez-vous l'activer ?")
            .setCancelable(false)
            .setPositiveButton("Oui") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Non") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
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
        mapFragment?.getMapAsync(this)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference
        binding = DataBindingUtil.bind(view.findViewById(R.id.infoCard))!!

        binding.hideCard.setOnClickListener { binding.root.visibility = View.GONE }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        checkGPSStatus()
        updateUserLocation()

        // Set up the choose type button
        val btnChooseType = view.findViewById<Button>(R.id.btnChooseType)
        btnChooseType.setOnClickListener {
            showChooseTypeDialog()
        }

        // Set up the seek bar
        val seekBarDistance = view.findViewById<SeekBar>(R.id.seekBarDistance)
        val tvDistanceLabel = view.findViewById<TextView>(R.id.tvDistanceLabel)
        seekBarDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        })

        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                radius = 9999999
                seekBarDistance.visibility = View.GONE
                tvDistanceLabel.visibility = View.GONE
            } else {
                radius = 50
                seekBarDistance.visibility = View.VISIBLE
                seekBarDistance.progress = 50
                tvDistanceLabel.visibility = View.VISIBLE
                tvDistanceLabel.text = "Radius (km): 50"
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val offre = marker.tag as? OffreModel
        if (offre != null) {
            showInfoCard(offre)
        }
        return true
    }

    private fun showInfoCard(offre: OffreModel) {
        binding.offre = offre
        binding.root.visibility = View.VISIBLE
    }

    private fun updateUserLocation() {
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
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                googleMap.addMarker(
                    MarkerOptions().position(currentLocation).title("Current Location")
                )
            }
        }
    }

    private fun showChooseTypeDialog() {
        val options = arrayOf("Utilisateurs à proximité", "Offres à proximité ")
        AlertDialog.Builder(requireContext())
            .setTitle("Que cherchez-vous ?")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> updateLocationManually()
                    1 -> updateOffersManually()
                }
            }
            .show()
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
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.clear() // Clear the map before adding new markers
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                googleMap.addMarker(
                    MarkerOptions().position(currentLocation).title("Current Location")
                )
                fetchNearbyUsers(currentLocation)
            }
        }
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
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.clear() // Clear the map before adding new markers
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                googleMap.addMarker(
                    MarkerOptions().position(currentLocation).title("Current Location")
                )
                fetchNearbyOffers(currentLocation)
            }
        }
    }

    private fun fetchNearbyUsers(currentUserLocation: LatLng) {
        val maxDistance = radius * 1000 // Convert km to meters
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val locationSnapshot = userSnapshot.child("location")
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java)
                    val username = userSnapshot.child("nom").getValue(String::class.java)
                    if (latitude != null && longitude != null) {
                        val userLocation = LatLng(latitude, longitude)
                        val distance = calculateDistance(
                            currentUserLocation.latitude, currentUserLocation.longitude,
                            userLocation.latitude, userLocation.longitude
                        )

                        if (distance <= maxDistance) {
                            val bitmap =
                                BitmapFactory.decodeResource(resources, R.drawable.map_marker_user)
                                    .scale(90, 90)
                            val markerOptions = MarkerOptions()
                                .position(userLocation)
                                .title(username ?: "")
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))

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

    private fun fetchNearbyOffers(currentUserLocation: LatLng) {
        val maxDistance = radius * 1000 // Convert km to meters
        database.child("offres").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (offreSnapshot in snapshot.children) {
                    val latitude = offreSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = offreSnapshot.child("longitude").getValue(Double::class.java)
                    val pictureUrl = offreSnapshot.child("pictureUrl").getValue(String::class.java)
                    val offrename = offreSnapshot.child("nameoffre").getValue(String::class.java)
                    val ooffre = offreSnapshot.getValue(OffreModel::class.java)
                    if (latitude != null && longitude != null && pictureUrl != null && offrename != null && ooffre != null) {
                        val offreLocation = LatLng(latitude, longitude)

                        val distance = calculateDistance(
                            currentUserLocation.latitude, currentUserLocation.longitude,
                            offreLocation.latitude, offreLocation.longitude
                        )

                        if (distance <= maxDistance) {
                            loadProfileImageAndAddMarker(
                                pictureUrl,
                                offreLocation,
                                offrename,
                                ooffre
                            )
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun loadProfileImageAndAddMarker(
        profileImageUrl: String,
        userLocation: LatLng,
        offrename: String,
        ooffre: OffreModel
    ) {
        Glide.with(this)
            .asBitmap()
            .load(profileImageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val circularBitmap = getCircularBitmapWithBorder(resource, 7)
                    val markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title(offrename)
                        .icon(BitmapDescriptorFactory.fromBitmap(circularBitmap))

                    val marker = googleMap.addMarker(markerOptions)
                    if (marker != null) {
                        marker.tag = ooffre
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle if needed
                }
            })
    }

    private fun getCircularBitmapWithBorder(bitmap: Bitmap, borderWidth: Int): Bitmap {
        val size = 100 // Define a consistent size for all images
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false)

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = 0xff424242.toInt()
        val paint = Paint()
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        val radius = size / 2f
        val borderPaint = Paint()

        // Draw the circular bitmap
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaledBitmap, rect, rect, paint)

        // Draw the border
        borderPaint.color = Color.rgb(0, 255, 216)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderWidth.toFloat()
        borderPaint.isAntiAlias = true
        canvas.drawCircle(radius, radius, radius - borderWidth / 2f, borderPaint)

        return output
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
