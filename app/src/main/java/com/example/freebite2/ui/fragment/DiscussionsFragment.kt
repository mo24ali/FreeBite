package com.example.freebite2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.freebite2.databinding.FragmentDiscussionsBinding

class DiscussionsFragment : Fragment() {
    private lateinit var binding: FragmentDiscussionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiscussionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // You can implement onViewCreated if needed
}
