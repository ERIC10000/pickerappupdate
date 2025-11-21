package com.example.kenwapwa


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class CreateAccountActivity : AppCompatActivity() {

    // 1. Track the current step
    private var currentStep = 1

    // 2. Declare Views
    private lateinit var step1Container: LinearLayout
    private lateinit var step2Container: LinearLayout
    private lateinit var step3Container: LinearLayout

    private lateinit var progressStep1: View
    private lateinit var progressStep2: View
    private lateinit var progressStep3: View

    private lateinit var btnNext: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var btnBackArrow: ImageButton

    // Image Upload Views
    private lateinit var btnUploadPhoto: MaterialButton
    private lateinit var ivProfilePreview: ImageView
    private lateinit var layoutUploadPlaceholder: LinearLayout

    // 3. Image Handling Variables
    private lateinit var imageUri: Uri // Stores the temp URI for camera
    private var selectedImageUri: Uri? = null // Stores the final URI to use

    // --- RESULT LAUNCHERS ---

    // 1. Launcher for Gallery/Files
    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showImagePreview(it)
        }
    }

    // 2. Launcher for Camera
    private val takePictureResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            imageUri?.let {
                selectedImageUri = it
                showImagePreview(it)
            }
        }
    }

    // 3. Launcher for Permissions (Camera)
    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        initViews()
        setupListeners()
        updateUI(1)
    }

    private fun initViews() {
        // Steps
        step1Container = findViewById(R.id.step1_container)
        step2Container = findViewById(R.id.step2_container)
        step3Container = findViewById(R.id.step3_container)

        // Progress
        progressStep1 = findViewById(R.id.progress_step1)
        progressStep2 = findViewById(R.id.progress_step2)
        progressStep3 = findViewById(R.id.progress_step3)

        // Buttons
        btnNext = findViewById(R.id.btn_next_complete)
        btnBack = findViewById(R.id.btn_previous)
        btnBackArrow = findViewById(R.id.btn_back)

        // Image Views
        btnUploadPhoto = findViewById(R.id.btn_upload_photo)
        ivProfilePreview = findViewById(R.id.iv_profile_preview)
        layoutUploadPlaceholder = findViewById(R.id.layout_upload_placeholder)
    }

    private fun setupListeners() {
        // --- IMAGE UPLOAD LOGIC ---
        btnUploadPhoto.setOnClickListener {
            showImageSourceDialog()
        }

        // Also allow clicking the placeholder area
        layoutUploadPlaceholder.setOnClickListener {
            showImageSourceDialog()
        }

        // --- NAVIGATION LOGIC ---
        btnNext.setOnClickListener {
            when (currentStep) {
                1 -> if (validateStep1()) { currentStep = 2; updateUI(currentStep) }
                2 -> if (validateStep2()) { currentStep = 3; updateUI(currentStep) }
                3 -> {
                    Toast.makeText(this, "Account Created!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        btnBack.setOnClickListener {
            if (currentStep > 1) { currentStep--; updateUI(currentStep) }
        }

        btnBackArrow.setOnClickListener {
            if (currentStep > 1) { currentStep--; updateUI(currentStep) } else finish()
        }
    }

    // --- IMAGE HELPER FUNCTIONS ---

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Upload Profile Picture")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermissionAndOpen() // Take Photo
                1 -> selectImageFromGalleryResult.launch("image/*") // Gallery
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        // Create a temporary file to store the image
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        // Get the URI using the FileProvider we set up in Manifest
        imageUri = FileProvider.getUriForFile(applicationContext, "${packageName}.provider", tmpFile)

        // Launch camera
        takePictureResult.launch(imageUri)
    }

    private fun showImagePreview(uri: Uri) {
        // Hide placeholder, show image
        layoutUploadPlaceholder.visibility = View.GONE
        ivProfilePreview.visibility = View.VISIBLE
        ivProfilePreview.setImageURI(uri)

        // Change button text
        btnUploadPhoto.text = "Change Photo"
    }

    // --- NAVIGATION UI LOGIC ---

    override fun onBackPressed() {
        if (currentStep > 1) { currentStep--; updateUI(currentStep) } else super.onBackPressed()
    }

    private fun updateUI(step: Int) {
        val colorActive = ContextCompat.getColor(this, R.color.primary_green)
        val colorInactive = ContextCompat.getColor(this, R.color.light_gray)

        step1Container.visibility = View.GONE
        step2Container.visibility = View.GONE
        step3Container.visibility = View.GONE

        when (step) {
            1 -> {
                step1Container.visibility = View.VISIBLE
                btnBack.visibility = View.GONE
                btnNext.text = "Next"
                progressStep2.setBackgroundColor(colorInactive)
                progressStep3.setBackgroundColor(colorInactive)
            }
            2 -> {
                step2Container.visibility = View.VISIBLE
                btnBack.visibility = View.VISIBLE
                btnNext.text = "Next"
                progressStep2.setBackgroundColor(colorActive)
                progressStep3.setBackgroundColor(colorInactive)
            }
            3 -> {
                step3Container.visibility = View.VISIBLE
                btnBack.visibility = View.VISIBLE
                btnNext.text = "Complete"
                progressStep2.setBackgroundColor(colorActive)
                progressStep3.setBackgroundColor(colorActive)
            }
        }
    }

    private fun validateStep1(): Boolean {
        val firstName = findViewById<TextInputEditText>(R.id.et_first_name)
        if (firstName.text.isNullOrEmpty()) { firstName.error = "Required"; return false }
        return true
    }

    private fun validateStep2(): Boolean {
        val phone = findViewById<TextInputEditText>(R.id.et_phone)
        if (phone.text.isNullOrEmpty()) { phone.error = "Required"; return false }
        return true
    }
}