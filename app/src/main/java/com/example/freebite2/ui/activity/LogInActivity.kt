package com.example.freebite2.ui.activity


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import com.example.freebite2.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException


class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    /*private lateinit var firebaseDB: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.backL.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

        val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
        val checkbox = preferences.getString("remember", "")

        if (checkbox == "true") {
            val intent = Intent(this, MainHomeActivity::class.java)
            startActivity(intent)
        } else if (checkbox == "false") {
            Toast.makeText(this, "Vauillez s'inscrire SVP!.", Toast.LENGTH_SHORT).show()
        }

        binding.lgBtn.setOnClickListener {
            val intent = Intent(this, MainHomeActivity::class.java)
            startActivity(intent)
        }

        binding.rememberBtn.setOnCheckedChangeListener { _, isChecked ->
            val editor = preferences.edit()
            if (isChecked) {
                editor.putString("remember", "true")
                Toast.makeText(this, "Je me souvient !", Toast.LENGTH_SHORT).show()
            } else {
                editor.putString("remember", "false")
            }
            editor.apply()
        }
        /*firebaseDB = FirebaseDatabase.getInstance()
        dbReference = firebaseDB.reference.child("users")*/
        binding.lgBtn.setOnClickListener{

            val logInMail = binding.mail.text.toString()
            val logInMdp = binding.password.text.toString()
            if(logInMail.isNotEmpty() && logInMdp.isNotEmpty()){
                logInUser(logInMail,logInMdp)
            }else{
                Toast.makeText(this,"Tout les champs sont obligatoires !",Toast.LENGTH_SHORT).show()
            }
        }
        binding.forgotPasswordTextView.setOnClickListener {
            val email = binding.mail.text.toString()
            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

    }
    /*private fun logInUser(mail:String, mdp : String){
        dbReference.orderByChild("email").equalTo(mail).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists()){
                    for(userSnapshot in datasnapshot.children){
                        val userData = userSnapshot.getValue(InscriptionData::class.java)
                        if(userData != null && userData.mdp == mdp){
                            Toast.makeText(this@LogInActivity,"Connecté !",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LogInActivity,DashboardActivity::class.java))
                            finish()
                            return
                        }
                        Toast.makeText(this@LogInActivity,"Connexion échoué",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError:DatabaseError) {
                Toast.makeText(this@LogInActivity,"Erreur ${databaseError.message}",Toast.LENGTH_SHORT).show()

            }

        })
    }*/
    private fun logInUser(mail:String, mdp : String){
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
                        Toast.makeText(this@LogInActivity, "Connexion échouée! veuillez réesayer.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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

   // fun onImageClick(view: View) {}
}