package com.kominfo_mkq.entago.data.remote.response

data class NotificationResponse(
    val success: Boolean,
    val message: String,
    val data: List<NotificationItemData>
)

data class NotificationItemData(
    val notificationId: Int,
    val title: String,
    val body: String,
    val type: String,
    val created_At: String,
    val is_Read: Boolean,
    val read_At: String?
)