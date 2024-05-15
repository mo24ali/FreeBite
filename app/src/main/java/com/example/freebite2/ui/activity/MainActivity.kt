package com.example.freebite2.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.R
import com.example.freebite2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize binding using View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Your remaining code
        binding.sInscBtn.setOnClickListener {
            val scInt = Intent(this@MainActivity, MainHomeActivity::class.java)
            startActivity(scInt)
            finish()
        }

        binding.button3.setOnClickListener {
            val conInt = Intent(this@MainActivity, LogInActivity::class.java)
            startActivity(conInt)
            finish()
        }
    }
}
