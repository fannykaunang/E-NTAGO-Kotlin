package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.PegawaiData
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModel() {

    var uiState by mutableStateOf<ProfileUiState>(ProfileUiState.Loading)
        private set

    fun loadFullProfile() {
        val pin = prefManager.getPin() ?: ""
        viewModelScope.launch {
            try {
                val response = apiService.getPegawai(pin)
                if (response.success) {
                    uiState = ProfileUiState.Success(response.data)
                } else {
                    uiState = ProfileUiState.Error(response.message)
                }
            } catch (e: Exception) {
                uiState = ProfileUiState.Error("Gagal memuat profil: ${e.message}")
            }
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val data: PegawaiData) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModelFactory(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(apiService, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}