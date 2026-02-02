package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.NotifDetailData
import kotlinx.coroutines.launch

sealed class NotifDetailUiState {
    object Loading : NotifDetailUiState()
    data class Success(val data: NotifDetailData) : NotifDetailUiState()
    data class Error(val message: String) : NotifDetailUiState()
}

class NotificationDetailViewModel(private val apiService: ApiService) : ViewModel() {
    var uiState: NotifDetailUiState by mutableStateOf(NotifDetailUiState.Loading)
        private set

    fun loadDetail(id: String) {
        viewModelScope.launch {
            uiState = NotifDetailUiState.Loading
            try {
                // API ini di C# harus mengambil PIN dari Claims JWT
                val response = apiService.getNotificationDetail(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    uiState = NotifDetailUiState.Success(response.body()!!.data!!)

                    if (!data.is_Read) {
                        markNotificationAsRead(id)
                    }
                } else {
                    uiState = NotifDetailUiState.Error("Data tidak ditemukan atau Anda tidak memiliki akses.")
                }
            } catch (e: Exception) {
                uiState = NotifDetailUiState.Error("Gangguan jaringan: ${e.message}")
            }
        }
    }

    private fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            try {
                // Panggil API secara background (silent)
                apiService.markAsRead(id)
                android.util.Log.d("NOTIF_DEBUG", "ID $id berhasil ditandai sudah dibaca")
            } catch (e: Exception) {
                android.util.Log.e("NOTIF_DEBUG", "Gagal tandai baca: ${e.message}")
            }
        }
    }
}

class NotificationDetailViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationDetailViewModel::class.java)) {
            return NotificationDetailViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}