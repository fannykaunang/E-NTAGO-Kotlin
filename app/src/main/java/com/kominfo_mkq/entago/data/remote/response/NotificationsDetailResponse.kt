package com.kominfo_mkq.entago.data.remote.response

import com.google.gson.annotations.SerializedName

data class NotificationsDetailResponse(
    val success: Boolean,
    val message: String,
    val data: NotifDetailData?
)

data class NotifDetailData(
    val id: Int,
    val title: String?,
    val body: String?,
    val type: String?,
    @SerializedName("created_At") val created_At: String?,
    @SerializedName("created_By") val created_By: String?,
    @SerializedName("is_Read") val is_Read: Boolean,
    @SerializedName("read_At") val read_At: String?
)