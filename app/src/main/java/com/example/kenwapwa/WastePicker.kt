package com.example.kenwapwa

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize

data class WastePicker(
    val first_name: String,
    val last_name: String,
    val reg_id: String,
    val mobile_number: String,
    val county: String,
    val email: String? = null,
    @SerialName("id_number")
    val id_number: String,
    val profile_image: String? = null,
    @SerialName("password")
    val password: String
) : Parcelable
