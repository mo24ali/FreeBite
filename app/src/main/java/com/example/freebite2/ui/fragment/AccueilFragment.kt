package com.example.freebite2.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.freebite2.R.layout.fragment_accueil
import com.example.freebite2.databinding.FragmentAccueilBinding


class AccueilFragment : Fragment(fragment_accueil){
    lateinit var binding: FragmentAccueilBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccueilBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onSearchClick()

    }
    private fun onSearchClick() {
        binding.btnSearch.setOnClickListener(){

          //  searchMvvm.searchMealDetail(binding.edSearch.text.toString(),context)

        }
    }

}
