package com.example.freebite2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freebite2.MapsActivity
import com.example.freebite2.databinding.ActivityAfterSignUpHelloBinding
import com.example.freebite2.NameViewModel
import com.google.firebase.auth.FirebaseAuth

class AfterSignUpHelloActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAfterSignUpHelloBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterSignUpHelloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        val displayName = user?.displayName

        if (!displayName.isNullOrEmpty()) {
            //val greetingText = getString(R.string.hello_user, displayName)
            val greetingText = getString(R.string.hello_user)
            val finalGreetingText = "$greetingText $displayName"
            binding.greetingTextView.text = finalGreetingText
        }

        // Create an instance of YourViewModel with fullName parameter
        val fullName : String = finalGreetingText // Replace this with the actual full name
        binding.viewModel = NameViewModel(fullName) // Set the ViewModel
        // Execute pending bindings
        binding.executePendingBindings()

        binding.verificationBtn.setOnClickListener{
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }
}
