package com.example.freebite2.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        preferences = getSharedPreferences("checkbox", MODE_PRIVATE)

        binding.lgBtn.setOnClickListener {
            val email = binding.mail.text.toString()
            val password = binding.password.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Tous les champs sont requis!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.forgotPasswordTextView.setOnClickListener {
            val email = binding.mail.text.toString()
            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(this, "Veuillez entrer votre email!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in successful
                    Toast.makeText(this, "Connecté avec succès!", Toast.LENGTH_SHORT).show()
                    val editor = preferences.edit()
                    if (binding.rememberBtn.isChecked) {
                        editor.putString("remember", "true")
                    } else {
                        editor.putString("remember", "false")
                    }
                    editor.apply()
                    val intent = Intent(this, MainHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Sign in failed
                    Toast.makeText(this, "Échec de la connexion! Veuillez réessayer.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Lien de réinitialisation du mot de passe envoyé à votre email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Impossible d'envoyer un e-mail de réinitialisation", Toast.LENGTH_SHORT).show()
                }
            }
    }
}