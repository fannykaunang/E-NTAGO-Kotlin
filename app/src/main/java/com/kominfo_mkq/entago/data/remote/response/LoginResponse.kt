package com.kominfo_mkq.entago.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: LoginData?
)

data class LoginData(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserData
)

data class UserData(
    @SerializedName("userid") val userid: Int,
    @SerializedName("email") val email: String,
    @SerializedName("pin") val pin: String,
    @SerializedName("skpdid") val skpdid: Int,
    @SerializedName("level") val level: Int,
    @SerializedName("deviceid") val deviceid: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String
)

data class BaseResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)