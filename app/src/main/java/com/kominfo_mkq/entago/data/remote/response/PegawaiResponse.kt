package com.kominfo_mkq.entago.data.remote.response

data class PegawaiResponse(
    val success: Boolean,
    val message: String,
    val data: PegawaiData
)

data class PegawaiData(
    val pegawai_nama: String,
    val pegawai_nip: String,
    val pegawai_pin: String,
    val pegawai_id: Int,
    val pegawai_status: Int,
    val tgl_mulai_kerja: String,
    val gender: Int,
    val jabatan: String,
    val skpd: String,
    val sotk: String,
    val pegawai_telp: String,
    val tempat_lahir: String,
    val tgl_lahir: String,
    val photo_path: String,
    val latitude: String?,
    val longitude: String?,
    val sn: String?,
    val deviceid: String?
)