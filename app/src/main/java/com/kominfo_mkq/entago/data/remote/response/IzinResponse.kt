package com.kominfo_mkq.entago.data.remote.response

data class IzinResponse(
    val success: Boolean,
    val message: String,
    val data: List<IzinItem>
)

data class IzinItem(
    val izin_Id: Int,
    val izin_Tgl: String,
    val izin_Jenis_Name: String,
    val kat_Izin_Nama: String,
    val izin_Catatan: String,
    val izin_Status: Int,
    val izin_Noscan_Time: String?
)