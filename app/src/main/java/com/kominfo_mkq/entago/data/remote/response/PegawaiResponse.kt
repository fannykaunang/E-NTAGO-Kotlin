package com.kominfo_mkq.entago.data.remote.response

import com.google.gson.annotations.SerializedName

data class PegawaiResponse(
    val success: Boolean,
    val message: String,
    val data: PegawaiData
)

data class PegawaiData(
    @SerializedName("pegawai_Id") val pegawai_id: Int,
    @SerializedName("pegawai_Nama") val pegawai_nama: String?, // Sesuaikan dengan JSON: pegawai_Nama
    @SerializedName("pegawai_Nip") val pegawai_nip: String?,   // Sesuaikan dengan JSON: pegawai_Nip
    @SerializedName("pegawai_Pin") val pegawai_pin: String?,
    @SerializedName("tempat_Lahir") val tempat_lahir: String?,
    @SerializedName("pegawai_Telp") val pegawai_telp: String?,
    @SerializedName("pegawai_Status") val pegawai_status: Int?,
    @SerializedName("tgl_Lahir") val tgl_lahir: String?,
    @SerializedName("tgl_Mulai_Kerja") val tgl_mulai_kerja: String?,
    @SerializedName("photo_Path") val photo_path: String?,

    //val pegawai_status: Int,
    //val tgl_mulai_kerja: String,
    val gender: Int,
    val jabatan: String,
    val skpd: String,
    val sotk: String,
    //val pegawai_telp: String,
    //val tempat_lahir: String,
    //val tgl_lahir: String,
    //val photo_path: String,
    val latitude: String?,
    val longitude: String?,
    val sn: String?,
    val deviceid: String?
)