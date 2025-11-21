package com.example.kenwapwa

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val signupButton = findViewById<MaterialButton>(R.id.btn_signup)
        val signinButton = findViewById<MaterialButton>(R.id.btn_login)


        signupButton.setOnClickListener {
            // Handle signup button click
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        signinButton.setOnClickListener {
            // Handle signin button click
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


    }
}