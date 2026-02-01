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

    var lastCheckin by mutableStateOf("--:--")
    var lastCheckout by mutableStateOf("--:--")

    // State untuk Status Mesin
    var isLoading by mutableStateOf(false)
        private set
    var isMachineOnline by mutableStateOf(false)
        private set

    // Update fungsi refresh agar mencakup status mesin
    fun refreshAllData(riwayatViewModel: RiwayatViewModel, onFinished: () -> Unit) {
        viewModelScope.launch {
            // Jalankan secara paralel agar lebih cepat
            val profileJob = launch { loadProfile() }
            val riwayatJob = launch { riwayatViewModel.fetchRiwayatData(refresh = true) }
            val machineJob = launch { checkDeviceStatus() } // Tambahkan ini

            profileJob.join()
            riwayatJob.join()
            machineJob.join()

            onFinished()
        }
    }

    fun checkDeviceStatus() {
        val skpdId = prefManager.getSkpdid()

        // 1. Log skpdid sebelum pemanggilan API
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
                    // 2. Log jumlah perangkat yang ditemukan
                    android.util.Log.i("DEVICE_MONITOR", "Sukses! Online: ${body?.online}, Offline: ${body?.offline}")

                    isMachineOnline = (body?.online ?: 0) > 0
                } else {
                    // 3. Log jika server memberikan error (misal 404 atau 500)
                    android.util.Log.w("DEVICE_MONITOR", "Server merespon dengan error: ${response.code()}")
                    isMachineOnline = false
                }
            } catch (e: Exception) {
                // 4. Log jika terjadi kesalahan jaringan/koneksi
                android.util.Log.e("DEVICE_MONITOR", "Terjadi Exception: ${e.message}")
                isMachineOnline = false
            } finally {
                isLoading = false
            }
        }
    }

    fun loadProfile() {
        val pin = prefManager.getPin() ?: ""
        viewModelScope.launch {
            try {
                val response = apiService.getPegawai(pin)
                if (response.success) {
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

                    try {
                        val todayResponse = apiService.getTodayCheckin()
                        if (todayResponse.isSuccessful) {
                            val body = todayResponse.body()
                            if (body?.success == true && body.data != null) {
                                // Data ada (User sudah absen)
                                lastCheckin = body.data.checkin ?: "--:--"
                                lastCheckout = body.data.checkout ?: "--:--"
                            } else {
                                // Berhasil konek, tapi success false (Kasus jarang)
                                lastCheckin = "--:--"
                                lastCheckout = "--:--"
                            }
                        } else if (todayResponse.code() == 404) {
                            // KHUSUS TANGGAL 1: Server merespon 404 (Belum ada data)
                            android.util.Log.d("ABSEN_INFO", "User belum absen hari ini (404)")
                            lastCheckin = "--:--"
                            lastCheckout = "--:--"
                        }
                    } catch (e: Exception) {
                        // Jika API today gagal, biarkan default --:--
                        lastCheckin = "--:--"
                        lastCheckout = "--:--"
                        android.util.Log.e("API_ERROR", "Gagal load today status: ${e.message}")
                    }

                    uiState = DashboardUiState.Success(response.data)
                }
            } catch (e: Exception) {
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

                // 1. Eksekusi API
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

                // 2. Ambil pesan dari field 'response' (sesuai backend Anda)
                val serverMsg = response.response ?: response.message ?: "Terjadi kesalahan"

                when (response.result) {
                    1 -> { // Sukses (Insert/Update)
                        absenMessage = serverMsg
                        if (response.success) onSuccess()
                    }
                    10 -> { // Device Tidak Cocok
                        absenMessage = "❌ $serverMsg\nReg: $deviceIdInPref\nReal: $currentHardwareId"
                        android.util.Log.e("ABSEN_ERROR", "Terdaftar: $deviceIdInPref | Real: $currentHardwareId")
                    }
                    2, 4, 5 -> { // Luar Jam Absen
                        absenMessage = "⚠️ $serverMsg"
                    }
                    7 -> { // Sudah Absen Pulang
                        absenMessage = "ℹ️ $serverMsg"
                    }
                    else -> { // Result 6, 8, atau lainnya
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
}