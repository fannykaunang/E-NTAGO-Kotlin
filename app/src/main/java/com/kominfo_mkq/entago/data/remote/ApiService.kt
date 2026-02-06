package com.kominfo_mkq.entago.data.remote

import com.kominfo_mkq.entago.data.remote.request.ChangePasswordRequest
import com.kominfo_mkq.entago.data.remote.request.CheckinRequest
import com.kominfo_mkq.entago.data.remote.request.CheckoutRequest
import com.kominfo_mkq.entago.data.remote.request.FcmTokenRequest
import com.kominfo_mkq.entago.data.remote.request.LoginRequest
import com.kominfo_mkq.entago.data.remote.request.RegisterDeviceRequest
import com.kominfo_mkq.entago.data.remote.request.RequestMigrationRequest
import com.kominfo_mkq.entago.data.remote.request.VerifyMigrationRequest
import com.kominfo_mkq.entago.data.remote.response.BaseResponse
import com.kominfo_mkq.entago.data.remote.response.CheckinResponse
import com.kominfo_mkq.entago.data.remote.response.CheckoutResponse
import com.kominfo_mkq.entago.data.remote.response.CutiNormatifResponse
import com.kominfo_mkq.entago.data.remote.response.DeviceMonitorResponse
import com.kominfo_mkq.entago.data.remote.response.IzinDetailData
import com.kominfo_mkq.entago.data.remote.response.IzinJenisResponse
import com.kominfo_mkq.entago.data.remote.response.IzinResponse
import com.kominfo_mkq.entago.data.remote.response.LoginResponse
import com.kominfo_mkq.entago.data.remote.response.NotificationResponse
import com.kominfo_mkq.entago.data.remote.response.NotificationsDetailResponse
import com.kominfo_mkq.entago.data.remote.response.PegawaiResponse
import com.kominfo_mkq.entago.data.remote.response.RegisterDeviceResponse
import com.kominfo_mkq.entago.data.remote.response.RekapBulananResponse
import com.kominfo_mkq.entago.data.remote.response.RequestMigrationResponse
import com.kominfo_mkq.entago.data.remote.response.RiwayatResponse
import com.kominfo_mkq.entago.data.remote.response.SubmitIzinResponse
import com.kominfo_mkq.entago.data.remote.response.TodayCheckinResponse
import com.kominfo_mkq.entago.data.remote.response.TugasLuarResponse
import com.kominfo_mkq.entago.data.remote.response.TugasLuarUploadResponse
import com.kominfo_mkq.entago.data.remote.response.VerifyMigrationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
//    @POST("api/auth/login")
//    suspend fun login(
//        @Body request: LoginRequest
//    ): LoginResponse

    // ApiService.kt
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/pegawai/{pin}")
    suspend fun getPegawai(
        @Path("pin") pin: String
    ): PegawaiResponse

    @GET("api/checkin/{pin}")
    suspend fun getRiwayatAbsensi(
        @Path("pin") pin: String
    ): RiwayatResponse

    @GET("api/izin/pegawai/{pegawai_id}")
    suspend fun getIzinPegawai(
        @Path("pegawai_id") pegawaiId: Int
    ): IzinResponse?

    @GET("api/rekap-bulanan")
    suspend fun getRekapBulanan(
        @Query("year") year: Int,
        @Query("excludeWeekend") excludeWeekend: Boolean
    ): RekapBulananResponse

    @GET("api/tugas-luar")
    suspend fun getTugasLuar(): TugasLuarResponse

    @Multipart
    @POST("api/tugas-luar")
    suspend fun uploadTugasLuar(
        @Part("tujuan") tujuan: RequestBody,
        @Part("keterangan_tugas") keterangan: RequestBody,
        @Part("alamat") alamat: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part foto: MultipartBody.Part
    ): TugasLuarUploadResponse

    @Multipart
    @PUT("api/tugas-luar/{id}")
    suspend fun updateTugasLuar(
        @Path("id") id: Int,
        @Part("tujuan") tujuan: RequestBody,
        @Part("keterangan_tugas") keterangan: RequestBody,
        @Part("alamat") alamat: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): TugasLuarUploadResponse

    @POST("api/checkin")
    suspend fun postCheckin(
        @Header("X-Device-Id") deviceId: String, // Untuk Device Binding di Backend
        @Body request: CheckinRequest
    ): CheckinResponse

    @POST("api/checkout")
    suspend fun postCheckout(
        @Header("X-Device-Id") deviceId: String,
        @Body request: CheckoutRequest
    ): CheckoutResponse

    @GET("/api/checkin/today")
    suspend fun getTodayCheckin(): TodayCheckinResponse
    //@GET("/api/checkin/today")
    //suspend fun getTodayCheckin(): Response<TodayCheckinResponse>

    @PUT("api/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<BaseResponse>

    @GET("api/monitor/devices")
    suspend fun getDeviceStatus(
        @Query("skpdid") skpdId: Int
    ): Response<DeviceMonitorResponse>

    @PUT("api/pegawai/fcm-token")
    suspend fun updateFcmToken(
        @Body request: FcmTokenRequest
    ): Response<BaseResponse>

    @GET("api/notifications/{id}")
    suspend fun getNotificationDetail(@Path("id") id: String): Response<NotificationsDetailResponse>

    @PUT("api/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: String
    ): Response<BaseResponse>

    @PUT("api/pegawai/register-device")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest
    ): Response<RegisterDeviceResponse>

    // ApiService.kt
    @POST("api/pegawai/request-migration-otp")
    suspend fun requestMigrationOtp(
        @Body request: RequestMigrationRequest
    ): Response<RequestMigrationResponse>

//    @PUT("api/pegawai/migrate-device")
//    suspend fun migrateDevice(
//        @Body request: RegisterDeviceRequest, // Kita gunakan model yang sama (device_Id, device_Model, description)
//        @Query("otp") otp: String
//    ): Response<RegisterDeviceResponse>

    @POST("api/pegawai/verify-migration-otp")
    suspend fun verifyMigrationOtp(
        @Body request: VerifyMigrationRequest
    ): Response<VerifyMigrationResponse>

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("pegawai_id") pegawaiId: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): NotificationResponse

    @POST("api/notifications/read/{id}")
    suspend fun markAsRead(
        @Path("id") notificationId: Int
    ): BaseResponse

    // Ambil list Jenis & Kategori
    @GET("api/izin/jenis-kategori")
    suspend fun getJenisIzin(): IzinJenisResponse

    // Ambil list Cuti Normatif
    @GET("api/cuti/normatif")
    suspend fun getCutiNormatif(): CutiNormatifResponse

    // Submit Izin (Multipart)
    @Multipart
    @POST("api/izin")
    suspend fun submitIzin(
        @Part("izin_jenis_id") jenisId: RequestBody,
        @Part("kat_izin_id") kategId: RequestBody,
        //@Part("izin_tgl") tgl: RequestBody,
        //@Part("izin_tgl") tglList: List<RequestBody>,
        @Part tglList: List<MultipartBody.Part>,
        @Part("izin_catatan") catatan: RequestBody,
        @Part file: MultipartBody.Part?, // Bisa Null
        @Part("izin_no_scan_time") jam: RequestBody?   // Bisa Null (untuk Group B)
    ): SubmitIzinResponse

    @GET("api/izin/pegawai/{pegId}/urutan/{urutan}")
    suspend fun getIzinDetail(
        @Path("pegId") pegId: Int,
        @Path("urutan") urutan: Long
    ): IzinDetailData
}