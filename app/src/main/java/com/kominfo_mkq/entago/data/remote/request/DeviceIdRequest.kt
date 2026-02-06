package com.kominfo_mkq.entago.data.remote.request

import com.google.gson.annotations.SerializedName


// data/remote/request/RegisterDeviceRequest.kt
data class RegisterDeviceRequest(
    @SerializedName("device_Id") val device_Id: String,
    @SerializedName("device_Model") val device_Model: String,
    @SerializedName("description") val description: String = "Register dari Android"
)

data class RequestMigrationRequest(
    @SerializedName("device_Id") val device_Id: String,
    @SerializedName("device_Model") val device_Model: String
)

data class VerifyMigrationRequest(
    @SerializedName("code") val code: String,
    @SerializedName("device_Id") val device_Id: String,
@SerializedName("device_Model") val device_Model: String
)