package com.kominfo_mkq.entago.data.remote.response

import com.google.gson.annotations.SerializedName

data class RekapBulananResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<RekapBulananData>
)

data class RekapBulananData(
    val pegawai_Id: Int,
    val pegawai_Pin: Int,
    val pegawai_Nama: String,
    val periode_Bulan: String, // format "2026-01"
    val total_Hari_Kerja: Int,
    val hadir: Int,
    val izin: Int,
    val alpa: Int,
    val total_Jam_Kerja: Double,
    val persentase_Kehadiran: Double
)