package com.kominfo_mkq.entago.data.remote

import com.kominfo_mkq.entago.data.remote.request.ChangePasswordRequest
import com.kominfo_mkq.entago.data.remote.request.CheckinRequest
import com.kominfo_mkq.entago.data.remote.request.CheckoutRequest
import com.kominfo_mkq.entago.data.remote.request.LoginRequest
import com.kominfo_mkq.entago.data.remote.response.BaseResponse
import com.kominfo_mkq.entago.data.remote.response.CheckinResponse
import com.kominfo_mkq.entago.data.remote.response.CheckoutResponse
import com.kominfo_mkq.entago.data.remote.response.DeviceMonitorResponse
import com.kominfo_mkq.entago.data.remote.response.IzinResponse
import com.kominfo_mkq.entago.data.remote.response.LoginResponse
import com.kominfo_mkq.entago.data.remote.response.PegawaiResponse
import com.kominfo_mkq.entago.data.remote.response.RekapBulananResponse
import com.kominfo_mkq.entago.data.remote.response.RiwayatResponse
import com.kominfo_mkq.entago.data.remote.response.TodayCheckinResponse
import com.kominfo_mkq.entago.data.remote.response.TugasLuarResponse
import com.kominfo_mkq.entago.data.remote.response.TugasLuarUploadResponse
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
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

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

//    @GET("/api/checkin/today")
//    suspend fun getTodayCheckin(): TodayCheckinResponse
    @GET("/api/checkin/today")
    suspend fun getTodayCheckin(): Response<TodayCheckinResponse>

    @PUT("api/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<BaseResponse>

    @GET("api/monitor/devices")
    suspend fun getDeviceStatus(
        @Query("skpdid") skpdId: Int
    ): retrofit2.Response<DeviceMonitorResponse>
}