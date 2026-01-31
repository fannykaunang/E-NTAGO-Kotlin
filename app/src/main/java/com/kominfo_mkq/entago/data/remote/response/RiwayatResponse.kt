package com.kominfo_mkq.entago.data.remote.response

data class RiwayatResponse(
    val success: Boolean,
    val message: String,
    val data: List<RiwayatItem>
)

data class RiwayatItem(
    val pegawai_Id: Int,
    val pegawai_Pin: Int,
    val scan_In: String,
    val scan_Out: String,
    val hari: String
)