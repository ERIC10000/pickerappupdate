package com.example.kenwapwa

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 1. The @Serializable annotation is MANDATORY for Supabase decoding
@Serializable
data class County(
    val id: String, // UUIDs can be treated as Strings
    val name: String,
    val code: String,

    // 2. Map SQL snake_case to Kotlin camelCase
    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)