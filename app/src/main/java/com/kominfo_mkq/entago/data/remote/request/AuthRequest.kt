package com.kominfo_mkq.entago.data.remote.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)