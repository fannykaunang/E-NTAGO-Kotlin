package com.kominfo_mkq.entago.data.remote.response

import com.google.gson.annotations.SerializedName

data class TugasLuarUploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    // Perhatikan: Ini bukan List, tapi Object (atau null)
    @SerializedName("data") val data: UploadDataDetail?
)

data class UploadDataDetail(
    @SerializedName("tugas_luar_id") val id: Int,
    @SerializedName("file_url") val fileUrl: String
)