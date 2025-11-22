package com.example.kenwapwa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class LoginActivity : AppCompatActivity() {
    // Supabase Client
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://gpnkysawiietekujccir.supabase.co",
        supabaseKey = "sb_publishable_XvU1mb48E8Qpneb8Ps9hmw_9nHbqTGc"
    ) {
        install(Postgrest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Initialize Views
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnLogin = findViewById<MaterialButton>(R.id.btn_login_submit)
        val linkSignUp = findViewById<TextView>(R.id.link_signup_from_login)
        val linkForgotPass = findViewById<TextView>(R.id.link_forgot_password)
        val etIdEmail = findViewById<TextInputEditText>(R.id.et_id_email)
        val etPassword = findViewById<TextInputEditText>(R.id.et_password)

        // 2. Handle Back Button
        btnBack.setOnClickListener { finish() }

        // 3. Handle Login Button
        btnLogin.setOnClickListener {
            val idEmail = etIdEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (idEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button and show loading
            btnLogin.isEnabled = false
            btnLogin.text = "Logging in..."

            lifecycleScope.launch {
                try {
                    // Query Supabase for user with matching ID/Email
                    val user = supabase.from("waste_pickers")
                        .select()
                        .decodeList<WastePicker>()
                        .firstOrNull { it.id_number == idEmail || it.email == idEmail }

                    // Check if user exists and password is correct
                    if (user != null && BCrypt.checkpw(password, user.password)) {
                        Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                        // Pass individual user fields to Dashboard
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                            putExtra("first_name", user.first_name)
                            putExtra("last_name", user.last_name)
                            putExtra("reg_id", user.reg_id)
                            putExtra("mobile_number", user.mobile_number)
                            putExtra("county", user.county)
                            putExtra("email", user.email)
                            putExtra("id_number", user.id_number)
                            putExtra("profile_image", user.profile_image)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Wrong ID/Email or Password", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("LoginActivity", "Login error", e)
                } finally {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                }
            }
        }

        // 4. Handle "Sign Up" Link
        linkSignUp.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        // 5. Handle "Forgot Password" Link
        linkForgotPass.setOnClickListener {
            Toast.makeText(this, "Please Contact the Administrator", Toast.LENGTH_SHORT).show()
        }
    }
}