package com.example.freebite2.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.freebite2.R
import com.example.freebite2.databinding.ActivityMainHomeBinding
import com.example.freebite2.ui.fragment.DiscussionsFragment
import com.example.freebite2.ui.fragment.AccueilFragment
import com.example.freebite2.ui.fragment.MapsFragment
import com.example.freebite2.ui.fragment.ProfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainHomeActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityMainHomeBinding

    private val accueilF = AccueilFragment()
    private val profilF = ProfilFragment()
    private val mapsF = MapsFragment()
    private val discussionsF = DiscussionsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNav = binding.bottomNavView

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_accueil -> setCurrentFragment(accueilF)
                R.id.nav_profil -> setCurrentFragment(profilF)
                R.id.nav_map -> setCurrentFragment(mapsF)
                R.id.nav_discussions -> setCurrentFragment(discussionsF)
            }
            true
        }

        // Show the first fragment on startup
        setCurrentFragment(accueilF)
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fcontainer, fragment)
            commit()
        }
    }
}
