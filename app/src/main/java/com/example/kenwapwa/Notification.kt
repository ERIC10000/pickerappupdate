package com.example.kenwapwa

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val recipient_type: String, // This will hold "all_waste_pickers" OR "Nairobi", etc.
    val recipient_id: String? = null,
    val created_at: String? = null,
    var isExpanded: Boolean = false, // For UI logic (expand/collapse)
    var isRead: Boolean = false      // For UI logic (red dot)
)
