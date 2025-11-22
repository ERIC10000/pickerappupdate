package com.example.kenwapwa

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView // CHANGED: Import AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MakeContributionActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etCounty: EditText
    // CHANGED: Define as AutoCompleteTextView, not Spinner
    private lateinit var spinnerPurpose: AutoCompleteTextView
    private lateinit var btnPay: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_contribution)

        initializeViews()
        setupData()
        setupListeners()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btn_back)
        etCounty = findViewById(R.id.et_county_readonly)
        // This will now work because the types match
        spinnerPurpose = findViewById(R.id.spinner_purpose)
        btnPay = findViewById(R.id.btn_pay_now)
    }

    private fun setupData() {
        // 1. Auto-Select County (Simulated)
        etCounty.setText("Nairobi")

        // 2. Populate Purpose Dropdown
        val purposes = listOf("Waste Collection Fee", "Recycling Support", "Community Donation", "Penalty Payment")

        // Use 'simple_dropdown_item_1line' for AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, purposes)

        // CHANGED: Use setAdapter() for AutoCompleteTextView
        spinnerPurpose.setAdapter(adapter)

        // Optional: Force the dropdown to open if they click it
        spinnerPurpose.setOnClickListener {
            spinnerPurpose.showDropDown()
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish() // Go back to Dashboard
        }
    }
}