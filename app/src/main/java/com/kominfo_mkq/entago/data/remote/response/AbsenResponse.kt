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
    val message: String?,  // ✅ UBAH KE NULLABLE untuk handle 404
    val data: TodayCheckinData?,  // ✅ Tetap nullable
    //val code: Int? = null  // ✅ TAMBAHKAN untuk debug
)

data class TodayCheckinData(
    val pegawai_Id: Int? = null,  // ✅ Nullable dengan default
    val tgl_Shift: String? = null,  // ✅ Nullable dengan default
    val checkin: String? = null,  // ✅ Nullable
    val checkout: String? = null  // ✅ Nullable
)