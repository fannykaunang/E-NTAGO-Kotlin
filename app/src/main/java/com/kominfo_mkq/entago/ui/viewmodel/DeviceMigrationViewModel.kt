package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.request.RequestMigrationRequest
import com.kominfo_mkq.entago.data.remote.request.VerifyMigrationRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeviceMigrationViewModel(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModel() {

    var uiState by mutableStateOf<MigrationUiState>(MigrationUiState.Idle)
        private set

    var maskedPhone by mutableStateOf("")

    var resendTimer by mutableStateOf(0)
        private set

    private var timerJob: Job? = null

    fun requestOtp(deviceId: String) {
        if (resendTimer > 0) return

        val model = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"

        viewModelScope.launch {
            uiState = MigrationUiState.SendingOtp
            try {
                val response = apiService.requestMigrationOtp(
                    RequestMigrationRequest(deviceId, model)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    maskedPhone = response.body()?.data?.phone_masked ?: ""
                    uiState = MigrationUiState.OtpSent

                    startResendTimer(60)
                } else {
                    uiState = MigrationUiState.Error(response.body()?.message ?: "Gagal mengirim OTP")
                }
            } catch (e: Exception) {
                uiState = MigrationUiState.Error("Koneksi Error: ${e.message}")
            }
        }
    }

    private fun startResendTimer(seconds: Int) {
        resendTimer = seconds
        timerJob?.cancel() // Batalkan timer lama jika ada
        timerJob = viewModelScope.launch {
            while (resendTimer > 0) {
                delay(1000)
                resendTimer--
            }
        }
    }

    // Pastikan timer berhenti jika ViewModel dihancurkan
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun verifyOtp(code: String, currentDeviceId: String) {
        val model = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"

        viewModelScope.launch {
            uiState = MigrationUiState.Verifying
            try {
                val response = apiService.verifyMigrationOtp(
                    VerifyMigrationRequest(
                        code = code,
                        device_Id = currentDeviceId,
                        device_Model = model
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    // Simpan ID baru di lokal setelah sukses verifikasi & migrasi di server
                    prefManager.updateLocalDeviceId(currentDeviceId)
                    uiState = MigrationUiState.Success
                } else {
                    // Menangkap pesan "Device_Id wajib diisi" atau "OTP salah" dari server
                    val errorBody = response.body()?.message ?: "Verifikasi gagal"
                    uiState = MigrationUiState.Error(errorBody)
                }

            } catch (e: Exception) {
                uiState = MigrationUiState.Error("Gagal verifikasi: ${e.message}")
            }
        }
    }
}

sealed class MigrationUiState {
    object Idle : MigrationUiState()
    object SendingOtp : MigrationUiState()
    object OtpSent : MigrationUiState()
    object Verifying : MigrationUiState()
    object Success : MigrationUiState()
    data class Error(val message: String) : MigrationUiState()
}

class DeviceMigrationViewModelFactory(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceMigrationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceMigrationViewModel(apiService, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}