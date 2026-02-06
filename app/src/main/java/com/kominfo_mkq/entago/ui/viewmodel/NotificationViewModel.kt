package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.NotificationItemData
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModel() {

    var notifications by mutableStateOf<List<NotificationItemData>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        val pegawaiId = prefManager.getPegawaiId()?.toString() ?: ""
        viewModelScope.launch {
            isLoading = true
            try {
                // Asumsi API menerima pegawai_id sebagai header atau param
                val response = apiService.getNotifications(pegawaiId, page = 1, pageSize = 20)
                if (response.success) {
                    notifications = response.data
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Gagal memuat data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun markAsReadLocally(notificationId: Int) {
        // Update list secara lokal agar UI langsung berubah
        notifications = notifications.map {
            if (it.notificationId == notificationId) {
                it.copy(is_Read = true) // Buat salinan data dengan status baru
            } else {
                it
            }
        }

        // Opsional: Panggil API ke backend C# agar tersimpan permanen di database
        viewModelScope.launch {
            try {
                apiService.markAsRead(notificationId)
            } catch (_: Exception) {
                // Log error jika diperlukan, tapi UI tetap sudah berubah
            }
        }
    }
}

class NotificationViewModelFactory(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(apiService, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}