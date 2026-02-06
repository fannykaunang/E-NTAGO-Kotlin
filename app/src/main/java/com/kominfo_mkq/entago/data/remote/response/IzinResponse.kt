package com.kominfo_mkq.entago.data.remote.response

data class IzinResponse(
    val success: Boolean,
    val message: String,
    val data: List<IzinItem>
)

data class IzinItem(
    val izin_Id: Int,
    val izin_Tgl: String,
    val izin_Urutan: Long,
    val izin_Jenis_Name: String,
    val kat_Izin_Nama: String,
    val izin_Catatan: String,
    val izin_Status: Int,
    val izin_Noscan_Time: String?
)

// 1. Model untuk Dropdown Jenis & Kategori Izin
data class IzinJenisResponse(
    val success: Boolean,
    val message: String,
    val data: List<IzinJenisItem>
)

data class IzinJenisItem(
    val izin_Jenis_Id: Int,
    val izin_Jenis_Name: String,
    val kategori: List<KategoriIzinItem>
)

data class KategoriIzinItem(
    val kat_Izin_Id: Int,
    val kat_Izin_Nama: String
)

// 2. Model untuk Dropdown Cuti Normatif
data class CutiNormatifResponse(
    val success: Boolean,
    val message: String,
    val data: List<CutiNormatifItem>
)

data class CutiNormatifItem(
    val cuti_N_Id: Int,
    val cuti_N_Nama: String
    // field lain diabaikan karena tidak dipakai untuk dropdown
)

// 3. Response setelah Submit
data class SubmitIzinResponse(
    val success: Boolean,
    val message: String
)

//data class IzinDetailResponse(
//    val success: Boolean,
//    val message: String,
//    val data: IzinDetailData?
//)

data class IzinDetailData(
    val pegawai_Id: Int,
    val izin_Urutan: Long,
    val izin_Jenis_Id: Int,
    val izin_Jenis_Name: String,
    val kat_Izin_Nama: String,
    val izin_Tgl_Pengajuan: String, // "2026-02-05T00:00:00"
    val izin_Tgl_Mulai: String,     // "2026-02-24T00:00:00"
    val izin_Tgl_Selesai: String,   // "2026-02-27T00:00:00"
    val izin_No_Scan_Time: String,
    val izin_Status: Int,           // 2
    val ket_Status: String?,
    val izin_Catatan: String?,
    val file_Path: String?
)