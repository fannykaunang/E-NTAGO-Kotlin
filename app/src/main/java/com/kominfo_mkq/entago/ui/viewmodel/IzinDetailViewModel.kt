package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.IzinDetailData
import kotlinx.coroutines.launch

class IzinDetailViewModel(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModel() {

    var uiState: IzinDetailUiState by mutableStateOf(IzinDetailUiState.Loading)
        private set

    fun fetchDetail(urutan: Long) {
        val pegId = prefManager.getPegawaiId() // Ambil ID Pegawai dari Prefs

        viewModelScope.launch {
            uiState = IzinDetailUiState.Loading
            try {
                val data = apiService.getIzinDetail(pegId, urutan)

                // Jika sampai baris ini, berarti Sukses (HTTP 200 OK)
                uiState = IzinDetailUiState.Success(data)
            } catch (e: Exception) {
                uiState = IzinDetailUiState.Error("Gagal memuat: ${e.message}")
            }
        }
    }
}

sealed class IzinDetailUiState {
    object Loading : IzinDetailUiState()
    data class Success(val data: IzinDetailData) : IzinDetailUiState()
    data class Error(val message: String) : IzinDetailUiState()
}

class IzinDetailViewModelFactory(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IzinDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IzinDetailViewModel(apiService, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}