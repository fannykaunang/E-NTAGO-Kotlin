package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.dao.TugasLuarDao
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.TugasLuarData
import kotlinx.coroutines.launch

class TugasLuarViewModel(
    private val apiService: ApiService,
    private val dao: TugasLuarDao
) : ViewModel() {
    var allTugas by mutableStateOf<List<TugasLuarData>>(emptyList())
    var filteredTugas by mutableStateOf<List<TugasLuarData>>(emptyList())
    var isLoading by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    var selectedTugas by mutableStateOf<TugasLuarData?>(null)

    fun fetchTugasLuar() {
        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Ambil data dari Room (Local)
                val offlineEntities = dao.getAllDrafts()

                // 2. Mapping Entity Room ke format TugasLuarData
                val offlineMappers = offlineEntities.map { entity ->
                    TugasLuarData(
                        id = entity.idLocal,            // Gunakan 'id', bukan 'tugasLuarId'
                        pegawaiId = 0,                  // Nilai dummy karena data lokal belum punya ID Pegawai
                        tanggal = "Menunggu Sinkron",    // Nilai dummy untuk tanggal
                        tujuan = entity.tujuan,
                        keterangan = entity.keterangan_tugas, // Parameter ini tadi terlewat
                        alamat = entity.alamat,
                        latitude = entity.latitude,     // Parameter ini tadi terlewat
                        longitude = entity.longitude,   // Parameter ini tadi terlewat
                        statusVerifikasi = 2,           // 2 berarti 'Menunggu'
                        lampiranPath = entity.imagePath, // Gunakan path gambar dari lokal
                        isOffline = true                // Penanda untuk badge UI
                    )
                }

                // 3. Ambil data dari API
                val response = apiService.getTugasLuar()
                if (response.success) {
                    // 4. Gabungkan: Offline di atas, API di bawah
                    val combinedList = offlineMappers + response.data
                    allTugas = combinedList
                    filteredTugas = combinedList
                }
            } catch (e: Exception) {
                // 1. Ambil data draft dari database lokal Room
                val offlineEntities = dao.getAllDrafts()

                // 2. Petakan setiap data Entity ke model TugasLuarData
                val offlineList = offlineEntities.map { entity ->
                    TugasLuarData(
                        id = entity.idLocal,             // ID lokal dari Room
                        pegawaiId = 0,                   // Belum ada ID dari server
                        tanggal = "Menunggu koneksi...",  // Keterangan waktu sementara
                        tujuan = entity.tujuan,
                        keterangan = entity.keterangan_tugas,
                        alamat = entity.alamat,
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        statusVerifikasi = 2,            // 2 = Status "Menunggu"
                        lampiranPath = entity.imagePath,
                        isOffline = true                 // Flag agar muncul badge "Offline"
                    )
                }

                // 3. Update state UI dengan data lokal saja karena API gagal
                allTugas = offlineList
                filteredTugas = offlineList

                android.util.Log.e("OFFLINE_MODE", "Gagal ambil API, menampilkan data lokal: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun onSearch(query: String) {
        searchQuery = query
        filteredTugas = if (query.isEmpty()) {
            allTugas
        } else {
            allTugas.filter {
                it.tujuan.contains(query, ignoreCase = true) ||
                        it.alamat.contains(query, ignoreCase = true)
            }
        }
    }
}

class TugasLuarViewModelFactory(
    private val apiService: ApiService,
    private val dao: TugasLuarDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TugasLuarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TugasLuarViewModel(apiService, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}