package com.kominfo_mkq.entago.ui.viewmodel

import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.request.RegisterDeviceRequest
import kotlinx.coroutines.launch

class DeviceActivationViewModel(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModel() {

    var uiState by mutableStateOf<ActivationUiState>(ActivationUiState.Idle)
        private set

    fun registerDevice(deviceId: String) {
        // Ambil Model HP Otomatis (Contoh: Samsung SM-A556E)
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

        viewModelScope.launch {
            uiState = ActivationUiState.Loading
            try {
                val response = apiService.registerDevice(
                    RegisterDeviceRequest(
                        device_Id = deviceId,
                        device_Model = deviceModel,
                        description = "Aktivasi mandiri dari aplikasi E-NTAGO"
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    // Update DeviceID di PrefManager lokal agar Dashboard tidak error
                    prefManager.updateLocalDeviceId(deviceId)
                    uiState = ActivationUiState.Success
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal mendaftarkan perangkat"
                    uiState = ActivationUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                uiState = ActivationUiState.Error("Koneksi terputus: ${e.message}")
            }
        }
    }
}

sealed class ActivationUiState {
    object Idle : ActivationUiState()
    object Loading : ActivationUiState()
    object Success : ActivationUiState()
    data class Error(val message: String) : ActivationUiState()
}

class DeviceActivationViewModelFactory(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceActivationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceActivationViewModel(apiService, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}