package com.example.freebite2.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.adapter.OffersAdapter
import com.example.freebite2.databinding.FragmentAccueilBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.activity.AddOffreActivity
import com.google.firebase.database.*

class AccueilFragment : Fragment() {

    private var _binding: FragmentAccueilBinding? = null
    private val binding get() = _binding!!
    private lateinit var offreAdapter: OffersAdapter
    private lateinit var offreList: MutableList<OffreModel>
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccueilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        offreList = mutableListOf()
        offreAdapter = OffersAdapter(offreList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = offreAdapter

        binding.addOffreButton.setOnClickListener {
            val addIntent = Intent(activity, AddOffreActivity::class.java)
            startActivity(addIntent)
        }

        database = FirebaseDatabase.getInstance().getReference("offres")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                offreList.clear()
                for (dataSnapshot in snapshot.children) {
                    val offre = dataSnapshot.getValue(OffreModel::class.java)
                    if (offre != null) {
                        offreList.add(offre)
                    }
                }
                offreAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Log.e("DatabaseError", error.message)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
