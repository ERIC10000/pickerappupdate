package com.example.kenwapwa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from

import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.launch
import java.io.File

class EditProfileActivity : AppCompatActivity() {
    // Supabase Client
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://gpnkysawiietekujccir.supabase.co",
        supabaseKey = "sb_publishable_XvU1mb48E8Qpneb8Ps9hmw_9nHbqTGc"
    ) {
        install(Postgrest)
        install(Storage)
    }

    // UI Components
    private lateinit var ivProfileImage: ImageView
    private lateinit var layoutProfilePlaceholder: LinearLayout
    private lateinit var btnChangePhoto: MaterialButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Form Fields
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etMobile: TextInputEditText
    private lateinit var etIdNumber: TextInputEditText
    private lateinit var etRegId: TextInputEditText
    private lateinit var spinnerCounty: AutoCompleteTextView

    // Variables
    private var selectedImageUri: Uri? = null
    private var currentProfileImageUrl: String? = null
    private var selectedCountyCode: String = ""

    // Image Picker Launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            updateProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Views
        initializeViews()
        setupCountyDropdown()
        setupListeners()

        // Get reg_id from intent
        val regId = intent.getStringExtra("reg_id") ?: ""
        if (regId.isNotEmpty()) {
            fetchUserData(regId)
        }
    }

    private fun initializeViews() {
        ivProfileImage = findViewById(R.id.iv_profile_image)
        layoutProfilePlaceholder = findViewById(R.id.layout_profile_placeholder)
        btnChangePhoto = findViewById(R.id.btn_change_photo)
        btnBack = findViewById(R.id.btn_back)
        btnCancel = findViewById(R.id.btn_cancel)
        btnSave = findViewById(R.id.btn_save)
        progressBar = findViewById(R.id.progress_bar)
        etFirstName = findViewById(R.id.et_first_name)
        etLastName = findViewById(R.id.et_last_name)
        etEmail = findViewById(R.id.et_email)
        etMobile = findViewById(R.id.et_mobile_number)
        etIdNumber = findViewById(R.id.et_id_number)
        etRegId = findViewById(R.id.et_reg_id)
        spinnerCounty = findViewById(R.id.spinner_county)
    }

    private fun setupCountyDropdown() {
        lifecycleScope.launch {
            try {
                val counties = supabase.from("counties").select().decodeList<County>()
                val countyNames = counties.map { it.name }
                val adapter = ArrayAdapter(this@EditProfileActivity, android.R.layout.simple_dropdown_item_1line, countyNames)
                spinnerCounty.setAdapter(adapter)
                spinnerCounty.setOnItemClickListener { _, _, position, _ ->
                    selectedCountyCode = counties[position].code
                }
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Failed to fetch counties", e)
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "Failed to load counties", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }
        btnChangePhoto.setOnClickListener { openGallery() }
        btnSave.setOnClickListener { saveProfile() }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun updateProfileImage(uri: Uri) {
        layoutProfilePlaceholder.visibility = View.GONE
        ivProfileImage.visibility = View.VISIBLE
        ivProfileImage.setImageURI(uri)
    }

    private fun fetchUserData(regId: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val user = supabase.from("waste_pickers")
                    .select()
                    .decodeList<WastePicker>()
                    .firstOrNull { it.reg_id == regId }

                user?.let {
                    runOnUiThread {
                        // Set user data to fields
                        etFirstName.setText(it.first_name)
                        etLastName.setText(it.last_name)
                        etEmail.setText(it.email)
                        etMobile.setText(it.mobile_number)
                        etIdNumber.setText(it.id_number)
                        etRegId.setText(it.reg_id)
                        spinnerCounty.setText(it.county, false)
                        currentProfileImageUrl = it.profile_image

                        // Load profile image if available
                        if (!it.profile_image.isNullOrEmpty()) {
                            Glide.with(this@EditProfileActivity)
                                .load(it.profile_image)
                                .circleCrop()
                                .placeholder(R.drawable.logo)
                                .error(R.drawable.logo)
                                .into(ivProfileImage)
                            ivProfileImage.visibility = View.VISIBLE
                            layoutProfilePlaceholder.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Failed to fetch user data", e)
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            } finally {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun saveProfile() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val mobile = etMobile.text.toString().trim()
        val idNumber = etIdNumber.text.toString().trim()
        val regId = etRegId.text.toString().trim()
        val county = spinnerCounty.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Upload new profile image if selected
                val imageUrl = selectedImageUri?.let { uploadImage(it) } ?: currentProfileImageUrl

                // Update user data in Supabase
                val updatedUser = mapOf(
                    "first_name" to firstName,
                    "last_name" to lastName,
                    "email" to email,
                    "mobile_number" to mobile,
                    "county" to county,
                    "profile_image" to imageUrl
                )

                // Correct Supabase update syntax with proper filter
                supabase.from("waste_pickers")
                    .update(updatedUser) {
                        filter {
                            eq("reg_id", regId)
                        }
                    }

                // Show success message
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                    val intent = Intent(this@EditProfileActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Failed to update profile", e)
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String? {
        return try {
            val tempFile = File.createTempFile("profile_${System.currentTimeMillis()}", ".jpg", cacheDir)
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Failed to create temp file")

            val imageName = "profile_${System.currentTimeMillis()}.jpg"
            supabase.storage["profile_images"].upload(
                path = imageName,
                file = tempFile,
                upsert = true
            )
            supabase.storage["profile_images"].publicUrl(imageName)
        } catch (e: Exception) {
            Log.e("EditProfileActivity", "Failed to upload image", e)
            null
        }
    }
}