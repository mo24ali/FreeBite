package com.example.freebite2.ui.activity


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException


class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.backL.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.rememberBtn.setOnCheckedChangeListener { _, isChecked ->
            val editor = preferences.edit()
            if (isChecked) {
                editor.putString("remember", "true")
                Toast.makeText(this, "Je me souviens!", Toast.LENGTH_SHORT).show()
            } else {
                editor.putString("remember", "false")
            }
            editor.apply()
        }

        binding.lgBtn.setOnClickListener {
            logInUser()
        }
    }

    private fun logInUser() {
        val mail = binding.mail.text.toString()
        val mdp = binding.pass.text.toString()

        if (mail.isEmpty() || mdp.isEmpty()) {
            Toast.makeText(this, "Tous les champs sont obligatoires !", Toast.LENGTH_SHORT).show()
            return
        }

        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(mail, mdp)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in successful
                    Toast.makeText(this@LogInActivity, "Connecté !", Toast.LENGTH_SHORT).show()

                    // Set "remember" preference to "true"
                    val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
                    val editor = preferences.edit()
                    editor.putString("remember", "true")
                    editor.apply()

                    // Proceed to dashboard activity
                    val intent = Intent(this@LogInActivity, MainHomeActivity::class.java)
                    startActivity(intent)
                } else {
                    // Sign in failed
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // Invalid credentials
                        Toast.makeText(this@LogInActivity, "Mot de passe ou email invalide !", Toast.LENGTH_SHORT).show()
                    } else {
                        // Other errors
                        Toast.makeText(this@LogInActivity, "Connexion échouée! Veuillez réessayer.", Toast.LENGTH_SHORT).show()
                    }
                    // Print detailed error message to console
                    task.exception?.printStackTrace()
                }
            }
    }

    private fun resetPassword(email: String) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Lien de mise à jour de mot de passe envoyé à votre e-mail", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Impossible d'envoyer un e-mail de réinitialisation", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
}