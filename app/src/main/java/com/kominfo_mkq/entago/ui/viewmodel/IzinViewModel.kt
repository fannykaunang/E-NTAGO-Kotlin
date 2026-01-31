package com.kominfo_mkq.entago.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.IzinItem
import kotlinx.coroutines.launch

class IzinViewModel(private val apiService: ApiService, private val prefManager: PrefManager) : ViewModel() {
    var uiState: IzinUiState by mutableStateOf(IzinUiState.Loading)
        private set

    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf("Semua")

    val filteredData: List<IzinItem>
        get() {
            val currentData = (uiState as? IzinUiState.Success)?.data ?: return emptyList()
            return currentData.filter { item ->
                val matchesSearch = item.kat_Izin_Nama.contains(searchQuery, ignoreCase = true) ||
                        item.izin_Catatan.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategory == "Semua" || item.izin_Jenis_Name == selectedCategory
                matchesSearch && matchesCategory
            }
        }

    // Mendapatkan daftar kategori unik dari data yang ada untuk menu filter
    val categories: List<String>
        get() {
            val currentData = (uiState as? IzinUiState.Success)?.data ?: return listOf("Semua")
            return listOf("Semua") + currentData.map { it.izin_Jenis_Name }.distinct()
        }

    init { fetchIzinList() }

    fun fetchIzinList() {
        viewModelScope.launch {
            uiState = IzinUiState.Loading
            try {
                val pegId = prefManager.getPegawaiId()

                // Log untuk memastikan pegId yang dikirim benar
                Log.d("IzinViewModel", "Mengambil data untuk pegawai_id: $pegId")

                val response = apiService.getIzinPegawai(pegId)

                if (response?.success ?: false) {
                    uiState = IzinUiState.Success(response?.data ?: emptyList())
                } else {
                    uiState = IzinUiState.Error(response?.message ?: "Gagal memuat data")
                }
            } catch (e: Exception) {
                Log.e("IzinViewModel", "Error fetching data", e)
                uiState = IzinUiState.Error("Terjadi kesalahan: ${e.localizedMessage}")
            }
        }
    }
}

sealed class IzinUiState {
    object Loading : IzinUiState()
    data class Success(val data: List<IzinItem>) : IzinUiState()
    data class Error(val message: String) : IzinUiState()
}

class IzinViewModelFactory(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IzinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IzinViewModel(apiService, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}