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


        setContentView(R.layout.activity_main)
      //  val inscription = findViewById<Button>(R.id.sInscBtn)
      //  val connexion = findViewById<Button>(R.id.button3)
        /*on va tester la reaction avec les vues*/
      //  inscription.setOnClickListener{ //setOnClickListener un listener utilisé pour gérer le clique sur le butons
            //Toast.makeText(this,"Vers l'inscription",Toast.LENGTH_LONG).show()//le contexte est toujours this
          binding.sInscBtn.setOnClickListener {
            val scInt = Intent( this@MainActivity, SignUpActivity::class.java)
            startActivity(scInt)
        }
       // connexion.setOnClickListener{
            //Toast.makeText(this,"Vers la connexion ",Toast.LENGTH_LONG).show()
         binding.button3.setOnClickListener {
            val conInt = Intent(this@MainActivity, LogInActivity::class.java)
            startActivity(conInt)
        }
    }
}