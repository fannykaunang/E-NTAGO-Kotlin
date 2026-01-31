package com.kominfo_mkq.entago.data.remote.response

import com.google.gson.annotations.SerializedName

data class TugasLuarResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<TugasLuarData>
)

data class TugasLuarData(
    @SerializedName("tugas_Luar_Id") val id: Int,
    @SerializedName("pegawai_Id") val pegawaiId: Int,
    @SerializedName("tugas_Tgl") val tanggal: String, // Format: 2026-01-12T13:40:05
    @SerializedName("tujuan") val tujuan: String,
    @SerializedName("keterangan_Tugas") val keterangan: String,
    @SerializedName("alamat") val alamat: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String,
    @SerializedName("is_Verified") val statusVerifikasi: Int, // 1: Verified, 2: Waiting
    @SerializedName("file_Path") val lampiranPath: String?,

    // Field lain opsional (bisa ditambahkan jika perlu ditampilkan)
    // @SerializedName("file_Name") val fileName: String?,
    val isOffline: Boolean = false
)