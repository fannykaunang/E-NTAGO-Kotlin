package com.kominfo_mkq.entago.data.remote.response

data class DeviceMonitorResponse(
    val success: Boolean,
    val message: String,
    val skpdid: Int,
    val online: Int,
    val offline: Int,
    val total: Int,
    val data: List<DeviceDetail>
)

data class DeviceDetail(
    val no: Int,
    val waktu: String,
    val ip_Address: String,
    val skpd_Alias: String,
    val device_Name: String,
    val status: String, // "ONLINE" atau "OFFLINE"
    val roundtripMs: Int?
)