package com.example.kenwapwa

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class CommunicationsActivity : AppCompatActivity() {

    // Supabase
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://gpnkysawiietekujccir.supabase.co",
        supabaseKey = "sb_publishable_XvU1mb48E8Qpneb8Ps9hmw_9nHbqTGc"
    ) {
        install(Postgrest)
    }

    private lateinit var adapter: NotificationsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnMarkAllRead: TextView
    private lateinit var btnBack: ImageButton

    // User Data
    private var userRegId: String = ""
    private var userCounty: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communications) // Ensure this layout matches the XML file name

        // 1. Get reg_id passed from Dashboard
        userRegId = intent.getStringExtra("reg_id") ?: ""

        // 2. Setup UI - FIND VIEW BY ID MUST MATCH XML
        btnBack = findViewById(R.id.btn_back)
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read)
        recyclerView = findViewById(R.id.recycler_notifications) // <--- THIS WAS THE ERROR

        // 3. Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificationsAdapter(mutableListOf())
        recyclerView.adapter = adapter

        // 4. Listeners
        btnBack.setOnClickListener { finish() }
        btnMarkAllRead.setOnClickListener {
            adapter.markAllRead()
            Toast.makeText(this, "Marked all as read", Toast.LENGTH_SHORT).show()
        }

        // 5. Fetch Data
        if (userRegId.isNotEmpty()) {
            fetchUserAndNotifications()
        } else {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserAndNotifications() {
        lifecycleScope.launch {
            try {
                // A. Fetch User details to get their County
                val user = supabase.from("waste_pickers")
                    .select()
                    .decodeList<WastePicker>()
                    .firstOrNull { it.reg_id == userRegId }

                if (user != null) {
                    userCounty = user.county

                    // B. Fetch All Notifications
                    val allNotifications = supabase.from("notifications")
                        .select()
                        .decodeList<Notification>()

                    // C. Filter Logic
                    val filteredList = allNotifications.filter { note ->
                        note.recipient_type == "all_waste_pickers" ||
                                note.recipient_type == "all_managers" ||
                                note.recipient_type.equals(userCounty, ignoreCase = true) || // County Match
                                note.recipient_id == userRegId
                    }.sortedByDescending { it.created_at }

                    runOnUiThread {
                        if (filteredList.isNotEmpty()) {
                            adapter.updateData(filteredList)
                        } else {
                            Toast.makeText(this@CommunicationsActivity, "No new messages", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CommsActivity", "Error", e)
                runOnUiThread {
                    Toast.makeText(this@CommunicationsActivity, "Error loading: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}