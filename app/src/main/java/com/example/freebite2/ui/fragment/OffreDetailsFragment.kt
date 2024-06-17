package com.example.freebite2.ui.fragment

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.FragmentOffreDetailsBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.activity.UserOffersActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class OffreDetailsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var offre: OffreModel
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private var _binding: FragmentOffreDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOffreDetailsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("offres")

        // Get the offer object from arguments
        offre = arguments?.getParcelable("offre")!!

        // Set up UI components

        managebutton(offre.providerID.toString())
        val offerPicDetails: ImageView = binding.offerPicDetails
        val offreName: TextView = binding.offreName
        val descriptionTextView: TextView = binding.descritptionRepas
        val durationTextView: TextView = binding.durationRepas
        val providerTextView: TextView = binding.providerTextView // Added providerTextView
        val providerUID = offre.providerID // Assuming you have the UID of the provider in offre model
        val usersReference = FirebaseDatabase.getInstance().getReference("Users").child(providerUID.toString())
        usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val providerName = dataSnapshot.child("nom").value.toString()
                    // Set provider name to TextView
                    providerTextView.text = providerName
                } else {
                    // Handle case where user information does not exist
                    providerTextView.text = "Unknown Provider"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Log.e("Firebase", "Error fetching provider name: ${databaseError.message}")
            }
        })
        mapFragment = childFragmentManager.findFragmentById(R.id.offerPosition) as SupportMapFragment
        val takeOfferBtn: Button = view.findViewById(R.id.takeOfferBtn)

        // Load offer picture using Glide
        Glide.with(this)
            .load(offre.pictureUrl)
            .into(offerPicDetails)

        // Set offer name
        offreName.text = offre.nameoffre

        // Set description
        descriptionTextView.text = offre.details

        durationTextView.text =offre.duration

        // Initialize map
        mapFragment.getMapAsync(this)

        binding.backBtn2.setOnClickListener {
            val accueilFragment = AccueilFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fprofil, accueilFragment) // Replace 'fprofil' with the id of your FrameLayout or the container for your fragments
                .addToBackStack(null)
                .commit()
        }
        // Set click listener for take offer button
        takeOfferBtn.setOnClickListener {
            takeOffer(offre)
        }
        binding.manageMyOffreBtn.setOnClickListener {
            val intent = Intent(activity, UserOffersActivity::class.java)
            startActivity(intent)

            /* val updateFragment = UpdateOffreFragment()
            val offer = offre
            val updateOffreFragment = UpdateOffreFragment()
            val bundle = Bundle()
            bundle.putParcelable("offre", offer)
            updateOffreFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.fhome, updateFragment) // Replace 'fprofil' with the id of your FrameLayout or the container for your fragments
                .addToBackStack(null)
                .commit()*/
        }

    }

    /*private fun takeOffer(offre: OffreModel) {
        // Remove offer from Firebase
        databaseReference.child(offre.offerID.toString()).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Offer taken!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
                // If you need to update UI or perform additional actions after taking the offer, do it here

                // Create a new notification in the Firebase database
                val notificationMessage = "User ${FirebaseAuth.getInstance().currentUser?.displayName} wants to take your offer ${offre.nameoffre}"
                createNotification(offre.providerID.toString(), notificationMessage)

            } else {
                Toast.makeText(context, "Failed to take offer. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }*/
    private fun takeOffer(offre: OffreModel) {
        // Do not remove offer from Firebase immediately
        // Instead, create a new notification in the Firebase database
        val notificationMessage = "User ${FirebaseAuth.getInstance().currentUser?.displayName} veut prendre ton repas ${offre.nameoffre}"
        Toast.makeText(context, "Demande envoyé!", Toast.LENGTH_SHORT).show()
        createNotification(offre.providerID.toString(), notificationMessage,offre)
    }

    private fun createNotification(userId: String, message: String,off:OffreModel) {
        // Create a new notification object
        val time = LocalDate.now().toString() + " "+ LocalTime.now().toString()
        val notification = mapOf(
            "message" to message,
            "timestamp" to time,
            "type" to "offer_taken",
            "OfferID" to off.offerID,
            "senderId" to FirebaseAuth.getInstance().currentUser?.uid.toString(),
        )

        // Add the notification to the Firebase database
        val notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications")
        notificationsRef.child(userId).push().setValue(notification).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Notification created successfully")
            } else {
                Log.e("Firebase", "Failed to create notification: ${task.exception?.message}")
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val offerLocation = LatLng(offre.latitude!!, offre.longitude!!)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.map_marker_logo).scale(90,90)
                            val markerOptions = MarkerOptions()
                                .position(offerLocation)
                                .title(offre.nameoffre)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        googleMap.addMarker(markerOptions)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(offerLocation, 15f))
    }
    private fun managebutton(providerUID : String){
        val userId = auth.currentUser?.uid
        if (userId.toString() == providerUID){
            binding.manageMyOffreBtn.visibility = View.VISIBLE
            binding.takeOfferBtn.visibility = View.GONE
        }else{
            binding.manageMyOffreBtn.visibility = View.GONE
            binding.takeOfferBtn.visibility = View.VISIBLE
        }
    }
}
