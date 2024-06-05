package com.example.freebite2.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.databinding.ActivityLogInBinding
import com.example.freebite2.model.User
import com.example.freebite2.ui.activity.admin.AdminActivity
import com.example.freebite2.util.SharedPreferencesUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()

        binding.backL.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.forgotPasswordTextView.setOnClickListener {
            resetPassword(binding.mail.text.toString())
        }

        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getString("password", "")
        if (savedEmail != "" && savedPassword != "") {
            binding.mail.setText(savedEmail)
            binding.passwordTxtLgnIn.setText(savedPassword)
            binding.rememberBtn.isChecked = true
        }

        binding.lgBtn.setOnClickListener {
            val email = binding.mail.text.toString()
            val pass = binding.passwordTxtLgnIn.text.toString()

            if (checkAllfield()) {
                // Save login information if "Remember Me" is checked
                if (binding.rememberBtn.isChecked) {
                    val editor = sharedPreferences.edit()
                    editor.putString("email", email)
                    editor.putString("password", pass)
                    editor.apply()
                } else {
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                }
                // Check if the user is an admin
                checkIfUserIsAdmin(email, pass)
            }
        }
    }

    private fun checkIfUserIsAdmin(email: String, password: String) {
        if (email == "admin0@freebite.com" && password == "admin0123" ) {
            loginAdmin(email, password)
        } else {
            loginRegularUser(email, password)
        }
    }

    private fun loginAdmin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    if (user != null) {
                        startActivity(Intent(this, AdminActivity::class.java))
                        finish()
                    } else {
                        binding.adminLoginError.text = "Invalid credentials"
                    }
                } else {
                    binding.adminLoginError.text = "Authentication failed."
                }
            }
    }

    private fun loginRegularUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    if (user != null) {
                        // Get a reference to the user in the database
                        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // Get user data from snapshot
                                val userData = dataSnapshot.getValue(User::class.java)
                                if (userData != null) {
                                    // Check if isSupprimer is false
                                    if (!userData.isSupprimer) {
                                        // Proceed with login
                                        Toast.makeText(this@LogInActivity, "Connexion réussie", Toast.LENGTH_SHORT).show()
                                        SharedPreferencesUtil.setUserLoggedIn(this@LogInActivity, true)
                                        startActivity(Intent(this@LogInActivity, MainHomeActivity::class.java))
                                        finish()
                                    } else {
                                        // User is marked as deleted, do not proceed with login
                                        Toast.makeText(this@LogInActivity, "Ce compte a été supprimé", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle possible errors.
                                Toast.makeText(baseContext, "Erreur de connexion", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } else {
                    // Handle the exceptions
                    when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(baseContext, "Mot de passe ou email incorrect", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(baseContext, "Erreur de connexion", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    private fun resetPassword(email: String) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Lien de mise à jour de mot de passe envoyé à ton email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Impossible d'envoyer un e-mail de réinitialisation", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAllfield(): Boolean {
        val email = binding.mail.text.toString()
        if (binding.mail.text.toString().isEmpty()) {
            binding.mail.error = "Ce champ est obligatoire"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.mail.error = "Vérifiez la structure de ton email"
            return false
        }
        // Password should be at least 8 characters
        if (binding.passwordTxtLgnIn.text.toString().isEmpty()) {
            binding.passwordLgnTextInputLayout.error = "Ce champ est obligatoire"
            return false
        }
        if (binding.passwordTxtLgnIn.length() < 8) {
            binding.passwordLgnTextInputLayout.error = "Le mot de passe doit contenir au moins 8 caractères"
            return false
        }
        return true
    }
}
