package com.example.freebite2.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.freebite2.databinding.FragmentOffreDetailsBinding
import com.example.freebite2.model.OffreModel
import com.google.firebase.database.*

class OffreDetailsFragment : Fragment() {

    private var _binding: FragmentOffreDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOffreDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the offer ID passed from the previous fragment
        val offerId: String? = arguments?.getString("offerId")

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("offres")

        // Retrieve offer details from Firebase based on the offer ID
        offerId?.let { id ->
            databaseReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val offer: OffreModel? = dataSnapshot.getValue(OffreModel::class.java)

                    // Bind the offer object to the layout if it's not null
                    offer?.let {
                        binding.apply {
                            this.offer = it // Set the offer object to the data variable defined in the layout
                            executePendingBindings() // Ensure data binding is executed immediately
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    // For simplicity, you can log the error
                    // You can also display an error message to the user
                    databaseError.toException().printStackTrace()
                }
            })
        }

        // Handle back button click
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
