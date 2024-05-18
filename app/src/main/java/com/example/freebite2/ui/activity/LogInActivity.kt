package com.example.freebite2.ui.activity


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import com.example.freebite2.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException


class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    /*private lateinit var firebaseDB: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set view binding
        binding = ActivityLogInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        binding.backL.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()

        binding.forgotPasswordTextView.setOnClickListener{
            resetPassword(binding.mail.text.toString())
        }

        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getString("password", "")
        if (savedEmail != "" && savedPassword != "") {
            binding.mail.setText(savedEmail)
            binding.passwordTxtLgnIn.setText(savedPassword)
            binding.rememberBtn.isChecked = true
        }
        binding.lgBtn.setOnClickListener{
           val email = binding.mail.text.toString()
            val pass = binding.passwordTxtLgnIn.text.toString()

            // if true meaning all fiels have been well settedd
            if(checkAllfield()){
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
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(baseContext, "Connexion réussie", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainHomeActivity::class.java))
                            //finish to destroy the activity
                            finish()
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
        }


    /*// Define SharedPreferences
private lateinit var sharedPreferences: SharedPreferences

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ...

    // Initialize SharedPreferences
    sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)

    // Check if there is saved login information
    val savedEmail = sharedPreferences.getString("email", "")
    val savedPassword = sharedPreferences.getString("password", "")
    if (savedEmail != "" && savedPassword != "") {
        binding.mail.setText(savedEmail)
        binding.passwordTxtLgnIn.setText(savedPassword)
        binding.rememberMeCheckbox.isChecked = true
    }

    binding.lgBtn.setOnClickListener {
        val email = binding.mail.text.toString()
        val pass = binding.passwordTxtLgnIn.text.toString()

        if (checkAllfield()) {
            // ...

            // Save login information if "Remember Me" is checked
            if (binding.rememberMeCheckbox.isChecked) {
                val editor = sharedPreferences.edit()
                editor.putString("email", email)
                editor.putString("password", pass)
                editor.apply()
            } else {
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
            }
        }
    }
}*/

    }



    private fun resetPassword(email: String) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Lien de mise à jour de mot de passe, est envoyé à ton email", Toast.LENGTH_SHORT).show()
                } else {
                        Toast.makeText(this, "Impossible d'envoyer un e-mail de réinitialisation", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun checkAllfield(): Boolean {
        val email =  binding.mail.text.toString()
        if(binding.mail.text.toString() == ""){
            binding.mail.error = "Ce champs est obligatoires"
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.mail.error = "Verifiez la structure de ton email"
            return false
        }
        //also note password should be at least a characters
        if(binding.passwordTxtLgnIn.text.toString()==""){
            binding.passwordLgnTextInputLayout.error = "Ce champs est obligatoires"
            binding.passwordLgnTextInputLayout.errorIconDrawable = null
            return false
        }
        if(binding.passwordTxtLgnIn.length() <= 7) {
            binding.passwordLgnTextInputLayout.error = "Le mot de passe doit contenir au moins 8 caractères"
            binding.passwordLgnTextInputLayout.errorIconDrawable = null
            return false
        }
        return true
    }

   // fun onImageClick(view: View) {}
}
