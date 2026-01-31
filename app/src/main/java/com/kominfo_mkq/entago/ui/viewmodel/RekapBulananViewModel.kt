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
                    // Set default ke bulan Januari (index 0)
                    selectedMonthData = allRekapData.firstOrNull()
                }
            } catch (e: Exception) {
                // Handle Error
            } finally {
                isLoading = false
            }
        }
    }

    fun selectMonth(monthIndex: Int) {
        // monthIndex 0 = Januari, 1 = Februari, dst.
        if (monthIndex < allRekapData.size) {
            selectedMonthData = allRekapData[monthIndex]
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