package com.baptistaz.taskwave.data.model.auth

import com.google.gson.annotations.SerializedName

/**
 *  system user -> admin, manager, or normal user.
 *
 * @property id_user Unique identifier of the user.
 * @property name Full name of the user.
 * @property username Username for display.
 * @property email Email address used for authentication.
 * @property password User's password.
 * @property profileType ("admin", "manager", "user").
 * @property photo URL or path to the user's profile picture.
 * @property phoneNumber phone number.
 * @property authId Supabase auth identifier (nullable).
 */
data class User(
    @SerializedName("id_user") val id_user: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("password") val password: String = "",
    @SerializedName("profiletype") val profileType: String = "",
    @SerializedName("photo") val photo: String = "",
    @SerializedName("phonenumber") val phoneNumber: String = "",
    @SerializedName("auth_id") val authId: String? = null
)
