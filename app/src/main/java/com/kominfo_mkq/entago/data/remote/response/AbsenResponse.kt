package com.kominfo_mkq.entago.data.remote.response

// Interface sebagai kontrak utama
interface BaseAbsenResponse {
    val success: Boolean
    val result: Int
    val response: String?
    val message: String?
}

// Implementasi untuk Checkin
data class CheckinResponse(
    override val success: Boolean,
    override val result: Int,
    override val response: String?,
    override val message: String?
) : BaseAbsenResponse

// Implementasi untuk Checkout
data class CheckoutResponse(
    override val success: Boolean,
    override val result: Int,
    override val response: String?,
    override val message: String?
) : BaseAbsenResponse

// Data class untuk menangkap response dari /api/checkin/today
data class TodayCheckinResponse(
    val success: Boolean,
    val message: String,
    val data: TodayCheckinData?
)

data class TodayCheckinData(
    val pegawai_Id: Int,
    val tgl_Shift: String,
    val checkin: String?,
    val checkout: String?
)