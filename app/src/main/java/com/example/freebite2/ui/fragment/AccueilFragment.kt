package com.example.freebite2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freebite2.adapter.OffreAdapter
import com.example.freebite2.databinding.FragmentAccueilBinding
import com.example.freebite2.model.OffreModel
import com.google.firebase.database.*

class AccueilFragment : Fragment() {

    private var _binding: FragmentAccueilBinding? = null
    private val binding get() = _binding!!
    private lateinit var offreAdapter: OffreAdapter
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
        offreAdapter = OffreAdapter(offreList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = offreAdapter

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
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
