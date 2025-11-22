package com.example.kenwapwa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import java.io.File

class CreateAccountActivity : AppCompatActivity() {
    // Supabase Client
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://gpnkysawiietekujccir.supabase.co",
        supabaseKey = "sb_publishable_XvU1mb48E8Qpneb8Ps9hmw_9nHbqTGc"
    ) {
        install(Postgrest)
        install(Storage)
    }

    // Step Tracking
    private var currentStep = 1
    private var selectedCountyCode: String = ""
    private lateinit var progressBar: ProgressBar

    // Views
    private lateinit var step1Container: LinearLayout
    private lateinit var step2Container: LinearLayout
    private lateinit var step3Container: LinearLayout
    private lateinit var progressStep1: View
    private lateinit var progressStep2: View
    private lateinit var progressStep3: View
    private lateinit var btnNext: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var btnBackArrow: ImageButton
    private lateinit var btnUploadPhoto: MaterialButton
    private lateinit var ivProfilePreview: ImageView
    private lateinit var layoutUploadPlaceholder: LinearLayout
    private lateinit var etRegId: TextInputEditText
    private lateinit var spinnerCountry: AutoCompleteTextView

    // Image Handling
    private lateinit var imageUri: Uri
    private var selectedImageUri: Uri? = null

    // Result Launchers
    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showImagePreview(it)
        }
    }

    private val takePictureResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            // imageUri is initialized in launchCamera() before this is called
            selectedImageUri = imageUri
            showImagePreview(imageUri)
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchCamera()
        else Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        initViews()
        setupListeners()
        updateUI(1)
        fetchCounties()

        // Setup the clickable links
        setupTermsAndPrivacyLinks()
    }

    private fun initViews() {
        step1Container = findViewById(R.id.step1_container)
        step2Container = findViewById(R.id.step2_container)
        step3Container = findViewById(R.id.step3_container)
        progressStep1 = findViewById(R.id.progress_step1)
        progressStep2 = findViewById(R.id.progress_step2)
        progressStep3 = findViewById(R.id.progress_step3)
        btnNext = findViewById(R.id.btn_next_complete)
        btnBack = findViewById(R.id.btn_previous)
        btnBackArrow = findViewById(R.id.btn_back)
        btnUploadPhoto = findViewById(R.id.btn_upload_photo)
        ivProfilePreview = findViewById(R.id.iv_profile_preview)
        layoutUploadPlaceholder = findViewById(R.id.layout_upload_placeholder)
        etRegId = findViewById(R.id.et_reg_id)
        spinnerCountry = findViewById(R.id.spinner_country)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupListeners() {
        btnUploadPhoto.setOnClickListener { showImageSourceDialog() }
        layoutUploadPlaceholder.setOnClickListener { showImageSourceDialog() }
        btnNext.setOnClickListener { handleNextButton() }
        btnBack.setOnClickListener { if (currentStep > 1) { currentStep--; updateUI(currentStep) } }
        btnBackArrow.setOnClickListener { if (currentStep > 1) { currentStep--; updateUI(currentStep) } else finish() }
    }

    // Function to handle clickable Terms and Privacy links
    private fun setupTermsAndPrivacyLinks() {
        val tvTermsPrivacy = findViewById<TextView>(R.id.tv_terms_privacy)
        val fullText = "By clicking Complete, you agree to our Terms of Service and Privacy Policy."
        val spannableString = SpannableString(fullText)

        // Define the Links
        val termsLink = "https://staging.kenwpwa.co.ke/KENAWPWA_TERMS_AND_CONDITIONS.pdf"
        val privacyLink = "https://staging.kenwpwa.co.ke/KENAWPWA_PRIVACY_POLICY.pdf"

        // Clickable Span for "Terms of Service"
        val termsClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(termsLink))
                    startActivity(browserIntent)
                } catch (e: Exception) {
                    Toast.makeText(this@CreateAccountActivity, "Could not open link", Toast.LENGTH_SHORT).show()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(this@CreateAccountActivity, R.color.primary_green)
            }
        }

        // Clickable Span for "Privacy Policy"
        val privacyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyLink))
                    startActivity(browserIntent)
                } catch (e: Exception) {
                    Toast.makeText(this@CreateAccountActivity, "Could not open link", Toast.LENGTH_SHORT).show()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(this@CreateAccountActivity, R.color.primary_green)
            }
        }

        // Locate the start and end indices
        val termsStart = fullText.indexOf("Terms of Service")
        val termsEnd = termsStart + "Terms of Service".length
        val privacyStart = fullText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        // Apply the spans
        if (termsStart >= 0) {
            spannableString.setSpan(termsClickable, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (privacyStart >= 0) {
            spannableString.setSpan(privacyClickable, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Set the text and enable links
        tvTermsPrivacy.text = spannableString
        tvTermsPrivacy.movementMethod = LinkMovementMethod.getInstance()
        tvTermsPrivacy.highlightColor = Color.TRANSPARENT
    }

    private fun handleNextButton() {
        when (currentStep) {
            1 -> if (validateStep1()) { currentStep = 2; updateUI(currentStep) }
            2 -> if (validateStep2()) { currentStep = 3; updateUI(currentStep) }
            3 -> registerUser()
        }
    }

    private fun registerUser() {
        progressBar.visibility = View.VISIBLE
        val firstName = findViewById<TextInputEditText>(R.id.et_first_name).text.toString()
        val lastName = findViewById<TextInputEditText>(R.id.et_last_name).text.toString()
        val email = findViewById<TextInputEditText>(R.id.et_email).text.toString()
        val idNumber = findViewById<TextInputEditText>(R.id.et_id_number).text.toString()
        val password = findViewById<TextInputEditText>(R.id.et_password).text.toString()
        val phone = findViewById<TextInputEditText>(R.id.et_phone).text.toString()
        val county = spinnerCountry.text.toString()

        lifecycleScope.launch {
            try {
                val imageUrl = selectedImageUri?.let { uploadImage(it) }
                val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                val regId = generateRegId(selectedCountyCode)
                val user = WastePicker(
                    first_name = firstName,
                    last_name = lastName,
                    reg_id = regId,
                    mobile_number = phone,
                    county = county,
                    email = email,
                    id_number = idNumber,
                    profile_image = imageUrl,
                    password = hashedPassword
                )
                supabase.from("waste_pickers").insert(user)
                Toast.makeText(this@CreateAccountActivity, "Account Created Redirecting to Login", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@CreateAccountActivity, LoginActivity::class.java))
            } catch (e: Exception) {
                Log.e("CreateAccountActivity", "Registration error", e)
                Toast.makeText(this@CreateAccountActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
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
            Log.e("CreateAccountActivity", "Failed to upload image", e)
            null
        }
    }

    private fun fetchCounties() {
        lifecycleScope.launch {
            try {
                val counties = supabase.from("counties").select().decodeList<County>()
                val countyNames = counties.map { it.name }
                val adapter = ArrayAdapter(this@CreateAccountActivity, android.R.layout.simple_dropdown_item_1line, countyNames)
                spinnerCountry.setAdapter(adapter)
                spinnerCountry.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    selectedCountyCode = counties[position].code
                    etRegId.setText(generateRegId(selectedCountyCode))
                }
            } catch (e: Exception) {
                Log.e("CreateAccountActivity", "Failed to fetch counties", e)
                Toast.makeText(this@CreateAccountActivity, "Failed to fetch counties", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateRegId(countyCode: String): String {
        val uniqueDigits = (1000..9999).random()
        return "WP/$countyCode/$uniqueDigits"
    }

    private fun showImageSourceDialog() {
        // FIXED: Added "Take Photo" and corrected index mapping
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Upload Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> selectImageFromGalleryResult.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        imageUri = FileProvider.getUriForFile(applicationContext, "${packageName}.provider", tmpFile)
        takePictureResult.launch(imageUri)
    }

    private fun showImagePreview(uri: Uri) {
        layoutUploadPlaceholder.visibility = View.GONE
        ivProfilePreview.visibility = View.VISIBLE
        ivProfilePreview.setImageURI(uri)
        btnUploadPhoto.text = "Change Photo"
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
        val lastName = findViewById<TextInputEditText>(R.id.et_last_name)
        val idNumber = findViewById<TextInputEditText>(R.id.et_id_number)
        val password = findViewById<TextInputEditText>(R.id.et_password)
        if (firstName.text.isNullOrEmpty()) { firstName.error = "Required"; return false }
        if (lastName.text.isNullOrEmpty()) { lastName.error = "Required"; return false }
        if (idNumber.text.isNullOrEmpty()) { idNumber.error = "Required"; return false }
        if (password.text.isNullOrEmpty()) { password.error = "Required"; return false }
        return true
    }

    private fun validateStep2(): Boolean {
        val phone = findViewById<TextInputEditText>(R.id.et_phone)
        val country = spinnerCountry
        if (phone.text.isNullOrEmpty()) { phone.error = "Required"; return false }
        if (country.text.isNullOrEmpty()) { country.error = "Required"; return false }
        return true
    }
}