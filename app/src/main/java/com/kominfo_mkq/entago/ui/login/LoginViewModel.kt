package com.kominfo_mkq.entago.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.request.FcmTokenRequest
import com.kominfo_mkq.entago.data.remote.request.LoginRequest
import kotlinx.coroutines.launch


class LoginViewModel(
    private val apiService: ApiService,
    private val prefManager: PrefManager // Helper untuk EncryptedSharedPreferences
) : ViewModel() {

    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    //fun resetState() {
    //    uiState = LoginUiState.Idle
    //}

    private fun updateFcmTokenToServer() {
        // 1. Ambil token dari Firebase SDK
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w("FCM_DEBUG", "Gagal mengambil token Firebase", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result

            viewModelScope.launch {
                try {
                    val response = apiService.updateFcmToken(FcmTokenRequest(token))
                    if (response.isSuccessful) {
                        android.util.Log.d("FCM_DEBUG", "FCM Token berhasil diperbarui di server")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FCM_DEBUG", "Gagal update token: ${e.message}")
                }
            }
        }
    }

    fun canUseBiometric(): Boolean {
        return prefManager.isBiometricEnabled() &&
                !prefManager.getBiometricEmail().isNullOrEmpty() &&
                !prefManager.getBiometricPass().isNullOrEmpty()
    }

    fun loginWithBiometric() {
        val email = prefManager.getBiometricEmail() ?: return
        val pass = prefManager.getBiometricPass() ?: return

        // Reset ke Idle dulu agar LaunchedEffect di UI mendeteksi perubahan state yang baru
        uiState = LoginUiState.Idle
        login(email, pass)
    }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        // Validasi input sederhana
        if (email.isEmpty() || password.isEmpty()) {
            uiState = LoginUiState.Error("Email dan password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            uiState = LoginUiState.Loading
            try {
                val response = apiService.login(LoginRequest(cleanEmail, cleanPassword))

                if (response.success && response.data != null) {
                    // 1. Ambil objek user dari response.data
                    val userData = response.data.user
                    val token = response.data.token

                    // 2. Perbaiki LOG (Akses harus melalui userData)
                    android.util.Log.d("LOGIN_API_DEBUG", "Data dari API - Lat: ${userData.latitude}, Lng: ${userData.longitude}")

                    // 3. Simpan ke PrefManager
                    prefManager.saveAuthData(
                        token = token,
                        user = userData
                    )
                    uiState = LoginUiState.Success

                    prefManager.saveBiometricCredentials(cleanEmail, cleanPassword)

                    updateFcmTokenToServer()
                } else {
                    uiState = LoginUiState.Error(response.message)
                }
            } catch (e: Exception) {
                uiState = LoginUiState.Error("Terjadi kesalahan jaringan: ${e.message}")
            }
        }
    }
}

class LoginViewModelFactory(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(apiService, prefManager) as T
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}