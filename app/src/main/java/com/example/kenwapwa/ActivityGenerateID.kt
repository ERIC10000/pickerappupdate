package com.example.kenwapwa

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActivityGenerateID : AppCompatActivity() {

    // Supabase Client
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://gpnkysawiietekujccir.supabase.co",
        supabaseKey = "sb_publishable_XvU1mb48E8Qpneb8Ps9hmw_9nHbqTGc"
    ) {
        install(Postgrest)
        install(Storage)
    }

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var btnScreenshot: MaterialButton
    private lateinit var btnExportPdf: MaterialButton

    // Containers to capture
    private lateinit var cardFrontContainer: FrameLayout
    private lateinit var cardBackContainer: FrameLayout

    // Front Fields
    private lateinit var tvNameFront: TextView
    private lateinit var tvIdNumberFront: TextView
    private lateinit var tvRegIdFront: TextView
    private lateinit var tvCountyFront: TextView
    private lateinit var tvValidUntilFront: TextView
    private lateinit var tvIssuedDateFront: TextView
    private lateinit var ivProfileImageFront: ImageView
    private lateinit var layoutProfilePlaceholder: LinearLayout

    // Back Fields
    private lateinit var ivQrCodeBack: ImageView
    private lateinit var tvSignatureName: TextView

    private var userNameForFile: String = "User"

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_id)

        initializeViews()
        setupListeners()

        val regId = intent.getStringExtra("reg_id") ?: ""
        if (regId.isNotEmpty()) {
            fetchUserData(regId)
        } else {
            Toast.makeText(this, "Error: No Registration ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btn_back)
        btnScreenshot = findViewById(R.id.btn_screenshot)
        btnExportPdf = findViewById(R.id.btn_export_pdf)

        // Capturable Views
        cardFrontContainer = findViewById(R.id.id_card_front)
        cardBackContainer = findViewById(R.id.id_card_back)

        // Front Views
        tvNameFront = findViewById(R.id.tv_id_card_name_front)
        tvIdNumberFront = findViewById(R.id.tv_id_card_id_number_front)
        tvRegIdFront = findViewById(R.id.tv_id_card_reg_id_front)
        tvCountyFront = findViewById(R.id.tv_id_card_county_front)
        tvValidUntilFront = findViewById(R.id.tv_id_card_valid_front)
        tvIssuedDateFront = findViewById(R.id.tv_id_card_issued_front)
        ivProfileImageFront = findViewById(R.id.iv_id_card_image_front)
        layoutProfilePlaceholder = findViewById(R.id.layout_id_image_placeholder_front)

        // Back Views
        ivQrCodeBack = findViewById(R.id.iv_qr_code_back)
        tvSignatureName = findViewById(R.id.tv_signature_name)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnScreenshot.setOnClickListener {
            // Capture both sides combined or just front?
            // For now, let's capture the FRONT as the main ID.
            // If you want to capture both, we'd need to merge bitmaps.
            val bitmap = getBitmapFromView(cardFrontContainer)
            saveBitmapToGallery(this, bitmap, "ID_FRONT_$userNameForFile")

            // Also save back
            val bitmapBack = getBitmapFromView(cardBackContainer)
            saveBitmapToGallery(this, bitmapBack, "ID_BACK_$userNameForFile")
        }

        btnExportPdf.setOnClickListener {
            val frontBitmap = getBitmapFromView(cardFrontContainer)
            val backBitmap = getBitmapFromView(cardBackContainer)
            createDoubleSidedPdf(frontBitmap, backBitmap, "ID_CARD_$userNameForFile")
        }
    }

    private fun fetchUserData(regId: String) {
        lifecycleScope.launch {
            try {
                val user = supabase.from("waste_pickers")
                    .select()
                    .decodeList<WastePicker>()
                    .firstOrNull { it.reg_id == regId }

                if (user != null) {
                    runOnUiThread { populateCard(user) }
                }
            } catch (e: Exception) {
                Log.e("GenerateID", "Fetch error", e)
            }
        }
    }

    private fun populateCard(user: WastePicker) {
        userNameForFile = "${user.first_name}_${user.last_name}"
        val fullName = "${user.first_name} ${user.last_name}".uppercase()

        // --- Populate Front ---
        tvNameFront.text = fullName
        tvIdNumberFront.text = user.id_number
        tvRegIdFront.text = user.reg_id
        tvCountyFront.text = user.county

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        tvIssuedDateFront.text = dateFormat.format(Date()).uppercase()

        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, 5)
        tvValidUntilFront.text = dateFormat.format(cal.time).uppercase()

        if (!user.profile_image.isNullOrEmpty()) {
            Glide.with(this).load(user.profile_image).centerCrop()
                .placeholder(R.drawable.logo).error(R.drawable.logo)
                .into(ivProfileImageFront)
            ivProfileImageFront.visibility = View.VISIBLE
            layoutProfilePlaceholder.visibility = View.GONE
        }

        // --- Populate Back ---

        // 1. Signature Text (Simulated signature using a script-like font or just italics)
        tvSignatureName.text = fullName

        // 2. Generate QR Code
        // This text pops up when scanned
        val verificationText = """
            VERIFIED MEMBER
            Name: $fullName
            ID No: ${user.id_number}
            Reg ID: ${user.reg_id}
            Status: BONAFIDE MEMBER of KeNAWPWA
            Valid: Active
        """.trimIndent()

        val qrBitmap = generateQRCode(verificationText)
        if (qrBitmap != null) {
            ivQrCodeBack.setImageBitmap(qrBitmap)
        }
    }

    // --- QR Code Generator ---
    private fun generateQRCode(content: String): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                512, 512
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- View to Bitmap ---
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    // --- Save Image (Scoped Storage) ---
    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                Toast.makeText(context, "Saved $filename to Gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Create 2-Page PDF (Front & Back) ---
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createDoubleSidedPdf(front: Bitmap, back: Bitmap, filename: String) {
        val pdfDocument = PdfDocument()

        // Page 1: Front
        val pageInfo1 = PdfDocument.PageInfo.Builder(front.width, front.height, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        page1.canvas.drawBitmap(front, 0f, 0f, Paint())
        pdfDocument.finishPage(page1)

        // Page 2: Back
        val pageInfo2 = PdfDocument.PageInfo.Builder(back.width, back.height, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        page2.canvas.drawBitmap(back, 0f, 0f, Paint())
        pdfDocument.finishPage(page2)

        // Save
        val name = "$filename.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        try {
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { stream ->
                    pdfDocument.writeTo(stream)
                }
                Toast.makeText(this, "PDF Saved to Downloads!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}