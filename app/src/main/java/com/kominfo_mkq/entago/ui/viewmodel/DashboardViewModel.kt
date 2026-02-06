package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.request.CheckinRequest
import com.kominfo_mkq.entago.data.remote.request.CheckoutRequest
import com.kominfo_mkq.entago.data.remote.response.BaseAbsenResponse
import com.kominfo_mkq.entago.data.remote.response.PegawaiData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModel() {

    var uiState by mutableStateOf<DashboardUiState>(DashboardUiState.Loading)
        private set

    // ===== FIX: GUNAKAN NON-NULLABLE STRING DENGAN DEFAULT VALUE =====
    var lastCheckin by mutableStateOf("--:--")
        private set

    var lastCheckout by mutableStateOf("--:--")
        private set

    // State untuk Status Mesin
    var isLoading by mutableStateOf(false)
        private set
    var isMachineOnline by mutableStateOf(false)
        private set

    var unreadNotificationCount by mutableStateOf(0)
        private set

    fun fetchUnreadCount() {
        val pegId = prefManager.getPegawaiId()?.toString() ?: ""
        viewModelScope.launch {
            try {
                // Kita ambil page 1 dengan size agak besar (misal 50) untuk menghitung unread
                val response = apiService.getNotifications(pegId, page = 1, pageSize = 50)
                if (response.success) {
                    unreadNotificationCount = response.data.count { !it.is_Read }
                }
            } catch (e: Exception) {
                android.util.Log.e("NOTIF_DEBUG", "Gagal ambil count: ${e.message}")
            }
        }
    }

    fun refreshAllData(
        riwayatViewModel: RiwayatViewModel,
        currentDeviceId: String,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            val profileJob = launch { loadProfile(currentDeviceId) }
            val riwayatJob = launch { riwayatViewModel.fetchRiwayatData(refresh = true) }
            val machineJob = launch { checkDeviceStatus() }
            val notifJob = launch { fetchUnreadCount() }

            profileJob.join()
            riwayatJob.join()
            machineJob.join()
            notifJob.join()

            onFinished()
        }
    }

    fun checkDeviceStatus() {
        val skpdId = prefManager.getSkpdid()

        android.util.Log.d("DEVICE_MONITOR", "Memulai pengecekan status. SKPD ID: $skpdId")

        if (skpdId == 0) {
            android.util.Log.e("DEVICE_MONITOR", "Error: SKPD ID tidak ditemukan (0).")
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.getDeviceStatus(skpdId)

                if (response.isSuccessful) {
                    val body = response.body()
                    android.util.Log.i("DEVICE_MONITOR", "Sukses! Online: ${body?.online}, Offline: ${body?.offline}")
                    isMachineOnline = (body?.online ?: 0) > 0
                } else {
                    android.util.Log.w("DEVICE_MONITOR", "Server merespon dengan error: ${response.code()}")
                    isMachineOnline = false
                }
            } catch (e: Exception) {
                android.util.Log.e("DEVICE_MONITOR", "Terjadi Exception: ${e.message}")
                isMachineOnline = false
            } finally {
                isLoading = false
            }
        }
    }

    fun loadProfile(currentDeviceId: String) {
        val pin = prefManager.getPin() ?: ""
        viewModelScope.launch {
            try {
                val response = apiService.getPegawai(pin)
                if (response.success) {
                    val serverDeviceId = response.data.deviceid?.trim() ?: ""

                    // --- PROTEKSI KEAMANAN: CEK INTEGRITAS PERANGKAT ---
                    if (serverDeviceId.isNotEmpty() && serverDeviceId != currentDeviceId) {
                        uiState = DashboardUiState.DeviceMismatch(serverDeviceId)
                        return@launch // Hentikan proses, jangan lanjut ke Dashboard
                    }

                    prefManager.savePegawaiProfile(
                        nama = response.data.pegawai_nama ?: "",
                        nip = response.data.pegawai_nip ?: "",
                        peg_id = response.data.pegawai_id,
                        lat = response.data.latitude,
                        lng = response.data.longitude,
                        sn = response.data.sn,
                        deviceId = response.data.deviceid,
                        skpd = response.data.skpd
                    )

                    // ===== FIX: ROBUST NULL HANDLING =====
                    try {
                        val todayResponse = apiService.getTodayCheckin()

                        //android.util.Log.d("TODAY_CHECKIN", "Response: success=${todayResponse.success}, data=${todayResponse.data}")

                        if (todayResponse.success && todayResponse.data != null) {
                            // ✅ Safe assignment dengan fallback
                            lastCheckin = todayResponse.data.checkin?.takeIf { it.isNotBlank() } ?: "--:--"
                            lastCheckout = todayResponse.data.checkout?.takeIf { it.isNotBlank() } ?: "--:--"
                            //android.util.Log.d("TODAY_CHECKIN", "Set values: checkin=$lastCheckin, checkout=$lastCheckout")
                        } else {
                            // ✅ Explicit fallback untuk response yang tidak success
                            lastCheckin = "--:--"
                            lastCheckout = "--:--"
                            //android.util.Log.d("TODAY_CHECKIN", "User belum absen hari ini (${todayResponse.code ?: "404"})")
                        }
                    } catch (_: retrofit2.HttpException) {
                        // ✅ Handle HTTP errors (404, 500, etc)
                        lastCheckin = "--:--"
                        lastCheckout = "--:--"
                        //android.util.Log.d("TODAY_CHECKIN", "User belum absen hari ini (${e.code()})")
                    } catch (_: Exception) {
                        // ✅ Handle parsing/network errors
                        lastCheckin = "--:--"
                        lastCheckout = "--:--"
                        //android.util.Log.e("API_ERROR", "Gagal load today status: ${e.message}", e)
                    }

                    uiState = DashboardUiState.Success(response.data)
                    fetchUnreadCount()
                }
            } catch (e: Exception) {
                // ✅ Pastikan lastCheckin/lastCheckout tidak null bahkan saat error
                lastCheckin = "--:--"
                lastCheckout = "--:--"
                uiState = DashboardUiState.Error(e.message ?: "Gagal memuat data")
            }
        }
    }

    var isAbsenLoading by mutableStateOf(false)
    var absenMessage by mutableStateOf<String?>(null)

    fun performAbsensi(currentHardwareId: String, inoutMode: Int, onSuccess: () -> Unit) {
        val deviceIdInPref = prefManager.getDeviceid() ?: ""
        val snFromServer = prefManager.getSn() ?: ""
        val pegId = prefManager.getPegawaiId()
        val pin = prefManager.getPin()?.toInt() ?: 0

        viewModelScope.launch {
            isAbsenLoading = true
            try {
                val localeId = Locale("id", "ID")
                val attIdFormatter = SimpleDateFormat("ddMMMMyyyyHHmmss", localeId)
                val generatedAttId = attIdFormatter.format(Date()) + snFromServer

//                val timeFormatter = SimpleDateFormat("HH:mm", localeId)
//                val currentTime = timeFormatter.format(Date())

                val response: BaseAbsenResponse = if (inoutMode == 1) {
                    val scanDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", localeId)
                    apiService.postCheckin(
                        currentHardwareId,
                        CheckinRequest(
                            pegawai_id = pegId, pin = pin, sn = snFromServer,
                            inoutmode = 1, att_id = generatedAttId,
                            scan_date = scanDateFormatter.format(Date())
                        )
                    )
                } else {
                    apiService.postCheckout(
                        currentHardwareId,
                        CheckoutRequest(
                            pin = pin, sn = snFromServer,
                            inoutmode = 2, att_id = generatedAttId
                        )
                    )
                }

                val serverMsg = response.response ?: response.message ?: "Terjadi kesalahan"

                when (response.result) {
                    1 -> {
                        absenMessage = serverMsg
                        if (response.success) {
                            onSuccess()
                        }
                    }
                    10 -> {
                        absenMessage = "❌ $serverMsg\nReg: $deviceIdInPref\nReal: $currentHardwareId"
                        android.util.Log.e("ABSEN_ERROR", "Terdaftar: $deviceIdInPref | Real: $currentHardwareId")
                    }
                    2, 4, 5 -> {
                        absenMessage = "⚠️ $serverMsg"
                    }
                    7 -> {
                        absenMessage = "ℹ️ $serverMsg"
                    }
                    else -> {
                        absenMessage = serverMsg
                    }
                }

            } catch (e: Exception) {
                absenMessage = "Masalah Koneksi: ${e.message}"
            } finally {
                isAbsenLoading = false
            }
        }
    }

    fun clearAbsenMessage() {
        absenMessage = null
    }
}

class DashboardViewModelFactory(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(apiService, prefManager) as T
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: PegawaiData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
    data class DeviceMismatch(val oldDeviceId: String) : DashboardUiState()
}