// src/main/java/com/example/freebite2/ui/activity/SplashActivity.kt
package com.example.freebite2.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.R
import com.example.freebite2.util.SharedPreferencesUtil

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (SharedPreferencesUtil.isUserLoggedIn(this)) {
                // User is logged in, navigate to MainHomeActivity
                val intent = Intent(this, MainHomeActivity::class.java)
                startActivity(intent)
            } else {
                // User is not logged in, navigate to SignupActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 3000) // 3 seconds delay
    }
}
