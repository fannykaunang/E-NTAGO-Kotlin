package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.RiwayatItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class RiwayatUiState {
    object Loading : RiwayatUiState()
    data class Success(val data: List<RiwayatItem>) : RiwayatUiState()
    data class Error(val message: String) : RiwayatUiState()
}

class RiwayatViewModel(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModel() {
    val pegawaiName = prefManager.getNama()
    val pegawaiNip = prefManager.getNip()

    // State Filter
    var searchQuery by mutableStateOf("")
    var startDate by mutableStateOf<Long?>(null)
    var endDate by mutableStateOf<Long?>(null)

    // State UI Utama
    var riwayatState: RiwayatUiState by mutableStateOf(RiwayatUiState.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var attendancePercentage by mutableStateOf("0%")
    var totalWorkHours by mutableStateOf("0j")

    // Data Internal
    private var allData = listOf<RiwayatItem>()
    private var displayCount = 15 // Start dengan 15 data

    // PERBAIKAN: Parser yang sesuai dengan format data dari API
    // Format dari API: "Senin, 13 Jan 2025 08:30"
    // Kita ambil bagian tanggal: "13 Jan 2025"
    private val dateParser = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    val filteredData: List<RiwayatItem>
        get() {
            // 1. Terapkan Filter dulu ke SEMUA data
            val filteredList = allData.filter { item ->
                // Filter Search Query
                val matchesQuery = searchQuery.isEmpty() ||
                        item.scan_In?.contains(searchQuery, ignoreCase = true) == true ||
                        item.scan_Out?.contains(searchQuery, ignoreCase = true) == true ||
                        item.hari?.contains(searchQuery, ignoreCase = true) == true

                // Filter Date Range
                val matchesDateRange = if (startDate != null && endDate != null) {
                    try {
                        // Ambil tanggal dari scan_In atau scan_Out
                        val scanData = if (!item.scan_In.isNullOrEmpty()) {
                            item.scan_In
                        } else {
                            item.scan_Out
                        }

                        if (scanData.isNullOrEmpty()) {
                            false
                        } else {
                            // Parse format: "Senin, 13 Jan 2025 08:30"
                            // Split by ", " → ["Senin", "13 Jan 2025 08:30"]
                            val parts = scanData.split(", ")
                            if (parts.size >= 2) {
                                // Ambil "13 Jan 2025 08:30" lalu ambil 3 kata pertama
                                val dateTimePart = parts[1]
                                val dateWords = dateTimePart.split(" ")

                                // Gabungkan 3 kata pertama: "13", "Jan", "2025"
                                if (dateWords.size >= 3) {
                                    val dateString = "${dateWords[0]} ${dateWords[1]} ${dateWords[2]}"

                                    // Parse dengan format "dd MMM yyyy"
                                    val itemDate = dateParser.parse(dateString)

                                    if (itemDate != null) {
                                        val itemTime = itemDate.time

                                        // Normalisasi waktu ke midnight (00:00:00)
                                        val calendar = Calendar.getInstance()

                                        // Start date di awal hari (00:00:00)
                                        calendar.timeInMillis = startDate!!
                                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                                        calendar.set(Calendar.MINUTE, 0)
                                        calendar.set(Calendar.SECOND, 0)
                                        calendar.set(Calendar.MILLISECOND, 0)
                                        val startTime = calendar.timeInMillis

                                        // End date di akhir hari (23:59:59)
                                        calendar.timeInMillis = endDate!!
                                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                                        calendar.set(Calendar.MINUTE, 59)
                                        calendar.set(Calendar.SECOND, 59)
                                        calendar.set(Calendar.MILLISECOND, 999)
                                        val endTime = calendar.timeInMillis

                                        // Normalisasi item date juga
                                        calendar.time = itemDate
                                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                                        calendar.set(Calendar.MINUTE, 0)
                                        calendar.set(Calendar.SECOND, 0)
                                        calendar.set(Calendar.MILLISECOND, 0)
                                        val normalizedItemTime = calendar.timeInMillis

                                        // Check if in range
                                        normalizedItemTime in startTime..endTime
                                    } else {
                                        false
                                    }
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        }
                    } catch (e: Exception) {
                        // Jika parsing gagal, anggap tidak match
                        android.util.Log.e("RiwayatViewModel", "Error parsing date: ${e.message}")
                        false
                    }
                } else {
                    true // Tidak ada filter tanggal, semua lolos
                }

                matchesQuery && matchesDateRange
            }

            // 2. Potong sesuai pagination
            return filteredList.take(displayCount)
        }

    init {
        fetchRiwayatData()
    }

    fun fetchRiwayatData(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) isRefreshing = true else if (allData.isEmpty()) riwayatState = RiwayatUiState.Loading

            try {
                val pin = prefManager.getPin().toString()
                val response = apiService.getRiwayatAbsensi(pin)

                if (response.success) {
                    allData = response.data
                    riwayatState = RiwayatUiState.Success(filteredData) // Update UI
                } else {
                    riwayatState = RiwayatUiState.Error(response.message)
                }

                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val rekapResponse = apiService.getRekapBulanan(year = currentYear, excludeWeekend = true)

                if (rekapResponse.success && rekapResponse.data.isNotEmpty()) {
                    // Ambil format bulan sekarang (misal: "2026-01")
                    val currentMonthStr = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

                    // Cari data bulan ini di dalam list
                    val statsBulanIni = rekapResponse.data.find { it.periode_Bulan == currentMonthStr }

                    if (statsBulanIni != null) {
                        // Update state untuk dashboard
                        attendancePercentage = String.format(Locale.US, "%.2f%%", statsBulanIni.persentase_Kehadiran)
                        totalWorkHours = String.format(Locale.US, "%.2fJ", statsBulanIni.total_Jam_Kerja)
                    }
                }
            } catch (e: Exception) {
                riwayatState = RiwayatUiState.Error("Gagal menghubungkan ke server: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }
    }

    fun loadMoreData() {
        // Cek apakah masih ada data yang belum ditampilkan DAN sedang tidak loading
        if (displayCount < allData.size && !isLoadingMore) {
            viewModelScope.launch {
                isLoadingMore = true
                delay(1000) // Simulasi loading agar user sadar ada proses ambil data
                displayCount += 10
                isLoadingMore = false
            }
        }
    }

    fun clearFilters() {
        searchQuery = ""
        startDate = null
        endDate = null
        displayCount = 15 // Reset pagination saat clear filter
    }
}

class RiwayatViewModelFactory(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RiwayatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RiwayatViewModel(apiService, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}