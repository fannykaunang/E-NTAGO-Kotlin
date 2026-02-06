package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.RekapBulananData
import kotlinx.coroutines.launch

class RekapBulananViewModel(private val apiService: ApiService) : ViewModel() {
    var allRekapData by mutableStateOf<List<RekapBulananData>>(emptyList())
    var selectedMonthData by mutableStateOf<RekapBulananData?>(null)
    var isLoading by mutableStateOf(false)

    fun fetchRekap(year: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.getRekapBulanan(year, true)
                if (response.success) {
                    allRekapData = response.data

                    // Logika dinamis mencari bulan berjalan
                    val sdf = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
                    val currentMonthStr = sdf.format(java.util.Date())

                    val currentData = allRekapData.find { it.periode_Bulan == currentMonthStr }

                    // SEKARANG: selectedMonthData hanya diisi oleh hasil pencarian bulan ini
                    selectedMonthData = currentData ?: allRekapData.lastOrNull()

                    android.util.Log.d("REKAP_DEBUG", "Fokus pada periode: ${selectedMonthData?.periode_Bulan}")
                }
            } catch (_: Exception) {
                // Handle Error
            } finally {
                isLoading = false
            }
        }
    }

    fun selectMonth(monthIndex: Int) {
        if (allRekapData.isNotEmpty() && monthIndex < allRekapData.size) {
            selectedMonthData = allRekapData[monthIndex]
        } else if (allRekapData.isNotEmpty()) {
            // Fallback ke data pertama jika bulan yang dicari belum ada
            selectedMonthData = allRekapData.firstOrNull()
        }
    }
}

class RekapBulananViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RekapBulananViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RekapBulananViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}