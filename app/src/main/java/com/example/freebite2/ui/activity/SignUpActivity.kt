package com.example.freebite2.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.freebite2.databinding.ActivitySignUpBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.example.freebite2.util.SharedPreferencesUtil

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.backS.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.sgnBtn.setOnClickListener {
            val signUpUsername = binding.pnom.text.trim().toString()
            val signUpUsername2 = binding.nom.text.trim().toString()
            val signUpMail = binding.mail.text.toString()
            val signUpMdp = binding.passTxtSgnUp.text.toString()
            if (signUpUsername.isNotEmpty() && signUpMail.isNotEmpty() && signUpMdp.isNotEmpty() && signUpUsername2.isNotEmpty()) {
                signUpUser(signUpUsername2, signUpUsername, signUpMail, signUpMdp)

            } else {
                Toast.makeText(this, "Tout les champs sont obligatoires !", Toast.LENGTH_SHORT).show()
            }

        }
    }
    private fun checkAllFields(): Boolean {
        val email = binding.mail.text.toString()
        val password = binding.passTxtSgnUp.text.toString()
        val confirmPassword = binding.confirmPassTxtSgnUp.text.toString()

        if (email == "") {
            binding.mail.error = "This is a required field"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.mail.error = "Check email format"
            return false
        }

        if (password == "") {
            binding.passTxtSgnUpLayout.error = "Ce champs est obligatoires"
            binding.passTxtSgnUpLayout.errorIconDrawable = null
            return false
        }

        if (password.length <= 7) {
            binding.passTxtSgnUpLayout.error = "Le mot de passe doit contenir au moins 8 caractères"
            binding.passTxtSgnUpLayout.errorIconDrawable = null
            return false
        }

        if (confirmPassword == "") {
            binding.confirmPassTxtSgnUpLayout.error = "Ce champs est obligatoires"
            binding.confirmPassTxtSgnUpLayout.errorIconDrawable = null
            return false
        }

        if (password != confirmPassword) {
            binding.confirmPassTxtSgnUpLayout.error = "Les mots de passe ne correspondent pas"
            binding.confirmPassTxtSgnUpLayout.errorIconDrawable = null
            return false
        }

        return true
    }

    private fun signUpUser(prenom: String, nom: String, email: String, mdp: String) {
        if(!checkAllFields()) {
            return
        }
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, mdp)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this@SignUpActivity, "Inscription réussie !", Toast.LENGTH_SHORT).show()
                    updateUserProfile(user!!, prenom, nom)
                    updateLocation(user)
                    SharedPreferencesUtil.setUserLoggedIn(this, true)
                    startActivity(Intent(this, AfterSignUpHelloActivity::class.java))
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this@SignUpActivity, "L'utilisateur existe déjà ! Veuillez se connecter.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SignUpActivity, "Inscription a échouée ! Réessayez.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun updateUserProfile(user: FirebaseUser, firstName: String, lastName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("$firstName $lastName")
            .build()

        user.updateProfile(profileUpdates)
    }

    private fun updateLocation(user: FirebaseUser) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        /*fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(user.displayName)
                        .build()

                    user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@SignUpActivity, "Bien localisé", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@SignUpActivity, "Mal localisé", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }*/
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val database = FirebaseDatabase.getInstance().getReference("Users")
                    val prenom = binding.pnom.text.trim().toString()
                    val nom = binding.nom.text.trim().toString()
                    val userData = mapOf(
                        "prenom" to prenom,
                        "nom" to nom,
                        "location" to mapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude
                        )
                    )
                    database.child(user.uid).setValue(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this@SignUpActivity, "Données bien enregistées", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@SignUpActivity, "Echec d'enregistrement des données", Toast.LENGTH_SHORT).show()
                        }
                }
            }

        /*fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val database = FirebaseDatabase.getInstance().getReference("Users")
                    val prenom = binding.pnom.text.toString()
                    val nom = binding.nom.text.toString()
                    val userData = mapOf(
                        "prenom" to prenom,
                        "nom" to nom,
                        "location" to mapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude
                        )
                    )
                    database.child(user.uid).setValue(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this@SignUpActivity, "Données bien enregistées", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@SignUpActivity, "Echec d'enregistrement des données", Toast.LENGTH_SHORT).show()
                        }
                }
            }*/
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }
}
