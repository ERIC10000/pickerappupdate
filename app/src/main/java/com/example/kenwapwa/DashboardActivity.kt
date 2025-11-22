package com.example.kenwapwa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Get user data from intent
        val firstName = intent.getStringExtra("first_name") ?: ""
        val lastName = intent.getStringExtra("last_name") ?: ""
        val regId = intent.getStringExtra("reg_id") ?: ""
        val mobileNumber = intent.getStringExtra("mobile_number") ?: ""
        val county = intent.getStringExtra("county") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val idNumber = intent.getStringExtra("id_number") ?: ""
        val profileImage = intent.getStringExtra("profile_image")

        // 2. Initialize Views
        val btnGenerateID = findViewById<MaterialButton>(R.id.generate_id)
        val btnEditProfile = findViewById<MaterialButton>(R.id.edit_profile)
        val btnMakeContribution = findViewById<MaterialButton>(R.id.btn_make_contribution)
        val btnNotifications = findViewById<View>(R.id.btn_notifications)
        val btnLogout = findViewById<ImageButton>(R.id.btn_logout)
        val ivProfileImage = findViewById<ImageView>(R.id.iv_profile_image)
        val layoutProfilePlaceholder = findViewById<View>(R.id.layout_profile_placeholder)

        // 3. Set user data to UI
        findViewById<TextView>(R.id.tv_full_name).text = "$firstName $lastName"
        findViewById<TextView>(R.id.tv_reg_id_header).text = regId
        findViewById<TextView>(R.id.tv_full_name_detail).text = "$firstName $lastName"
        findViewById<TextView>(R.id.tv_id_number).text = idNumber
        findViewById<TextView>(R.id.tv_county).text = county
        findViewById<TextView>(R.id.tv_mobile_number).text = mobileNumber
        findViewById<TextView>(R.id.tv_email).text = email

        // 4. Load profile image using Glide
        if (!profileImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileImage)
                .circleCrop()
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(ivProfileImage)
            ivProfileImage.visibility = View.VISIBLE
            layoutProfilePlaceholder.visibility = View.GONE
        } else {
            ivProfileImage.visibility = View.GONE
            layoutProfilePlaceholder.visibility = View.VISIBLE
        }

        // 5. Navigate to Generate ID
        btnGenerateID.setOnClickListener {
            val intent = Intent(this, ActivityGenerateID::class.java).apply {
                putExtra("reg_id", regId)
            }
            startActivity(intent)
        }

        // 6. Navigate to Edit Profile
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java).apply {
                putExtra("reg_id", regId)
            }
            startActivity(intent)
        }

        // 7. Navigate to Make Contribution
        btnMakeContribution.setOnClickListener {
            val intent = Intent(this, MakeContributionActivity::class.java).apply {
                putExtra("reg_id", regId)
            }
            startActivity(intent)
        }

        // 8. Navigate to Communications (Notifications)
        btnNotifications.setOnClickListener {
            val intent = Intent(this, CommunicationsActivity::class.java).apply {
                putExtra("reg_id", regId)
            }
            startActivity(intent)
        }

        // 9. Handle Logout
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}