package com.kominfo_mkq.entago.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.*
import com.kominfo_mkq.entago.utils.FileUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*

class IzinAddViewModel(private val apiService: ApiService) : ViewModel() {

    // --- STATE DATA MASTER ---
    var listJenisIzin by mutableStateOf<List<IzinJenisItem>>(emptyList())
    var listCutiNormatif by mutableStateOf<List<CutiNormatifItem>>(emptyList())
    var isLoadingMaster by mutableStateOf(false)

    // --- STATE FORM INPUT ---
    var selectedJenis by mutableStateOf<IzinJenisItem?>(null)
    var selectedKategori by mutableStateOf<KategoriIzinItem?>(null)
    var selectedCutiNormatif by mutableStateOf<CutiNormatifItem?>(null) // Dropdown tambahan

    var tanggalMulai by mutableStateOf<Long?>(null)
    var tanggalSelesai by mutableStateOf<Long?>(null)

    var inputJam by mutableStateOf("") // Format HH:mm
    var catatan by mutableStateOf("")
    var selectedFileUri by mutableStateOf<Uri?>(null)

    // --- STATE LOGIKA UI ---
    val showFileUploader: Boolean
        get() = listOf(40, 70, 71, 72, 80, 90).contains(selectedJenis?.izin_Jenis_Id)

    val showTimeInput: Boolean
        get() = listOf(73, 100, 101).contains(selectedJenis?.izin_Jenis_Id)

    val showCutiNormatifDropdown: Boolean
        get() = selectedJenis?.izin_Jenis_Id == 80

    // --- STATE SUBMIT ---
    var isSubmitting by mutableStateOf(false)
    var submitMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    init {
        loadMasterData()
    }

    private fun loadMasterData() {
        viewModelScope.launch {
            isLoadingMaster = true
            try {
                // Load Jenis Izin
                val resJenis = apiService.getJenisIzin()
                if (resJenis.success) listJenisIzin = resJenis.data

                // Load Cuti Normatif (Pre-load saja agar cepat saat ID 80 dipilih)
                val resNormatif = apiService.getCutiNormatif()
                if (resNormatif.success) listCutiNormatif = resNormatif.data
            } catch (_: Exception) {
                // Handle error silent
            } finally {
                isLoadingMaster = false
            }
        }
    }

    // Fungsi Submit dengan LOOPING
    fun submitForm(context: Context) {
        if (selectedJenis == null || selectedKategori == null || tanggalMulai == null || tanggalSelesai == null) {
            submitMessage = "Mohon lengkapi data wajib (Jenis, Kategori, Tanggal)."
            return
        }

        viewModelScope.launch {
            isSubmitting = true
            submitMessage = "Sedang memproses..."

            try {
                // 1. Siapkan Request Body Statis
                val jenisIdBody = selectedJenis!!.izin_Jenis_Id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val katIdBody = selectedKategori!!.kat_Izin_Id.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                // LOGIKA CUTI NORMATIF: Append ke catatan
                var finalCatatan = catatan
                if (showCutiNormatifDropdown && selectedCutiNormatif != null) {
                    finalCatatan = "$catatan (Jenis Cuti: ${selectedCutiNormatif!!.cuti_N_Nama})"
                }
                val catatanBody = finalCatatan.toRequestBody("text/plain".toMediaTypeOrNull())

                val jamBody = if (showTimeInput && inputJam.isNotEmpty()) {
                    inputJam.toRequestBody("text/plain".toMediaTypeOrNull())
                } else null

                // 2. Siapkan File (Sekali proses compress)
                var filePart: MultipartBody.Part? = null
                if (showFileUploader && selectedFileUri != null) {
                    val file = FileUtils.getFileFromUriIzin(context, selectedFileUri!!)
                    if (file != null) {
//                        val reqFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//                        filePart = MultipartBody.Part.createFormData("file", file.name, reqFile)
                        val mimeType = context.contentResolver.getType(selectedFileUri!!)
                            ?: "application/pdf"

                        val reqFile = file.asRequestBody(mimeType.toMediaType())
                        filePart = MultipartBody.Part.createFormData(
                            "file",
                            file.name,
                            reqFile
                        )
                    }
                }

                // 3. Generate List Tanggal
                val dateList = getDatesBetween(tanggalMulai!!, tanggalSelesai!!)
                //var successCount = 0

                val tglParts = dateList.map { dateString ->
                    MultipartBody.Part.createFormData("Izin_Tgl", dateString)
                }

                val response = apiService.submitIzin(
                    jenisId = jenisIdBody,
                    kategId = katIdBody,
                    tglList = tglParts, // Kirim list part ini
                    catatan = catatanBody,
                    file = filePart,
                    jam = jamBody
                )

                if (response.success) {
                    isSuccess = true
                    submitMessage = "Berhasil mengajukan izin untuk ${dateList.size} hari."
                } else {
                    isSuccess = false
                    submitMessage = "Gagal: ${response.message}"
                }

//                // 4. LOOPING API CALL
//                dateList.forEachIndexed { index, dateString ->
//                    submitMessage = "Mengirim data tanggal $dateString (${index + 1}/${dateList.size})..."
//                    val tglBody = dateString.toRequestBody("text/plain".toMediaTypeOrNull())
//
//                    val tglParts = dateList.map { dateStr ->
//                        MultipartBody.Part.createFormData("Izin_Tgl", dateStr) // Nama key harus sama persis dengan properti di C#
//                    }
//
//                    try {
//                        apiService.submitIzinBulk(
//                            jenisId = jenisIdBody,
//                            kategId = katIdBody,
//                            tglList = tglParts,
//                            catatan = catatanBody,
//                            file = filePart,
//                            jam = jamBody
//                        )
//                        successCount++
//                    } catch (e: Exception) {
//                        android.util.Log.e("SUBMIT_LOOP", "Gagal tgl $dateString: ${e.message}")
//                    }
//                }

//                val tglBodies = dateList.map {
//                    it.toRequestBody("text/plain".toMediaTypeOrNull())
//                }
//
//                val response = apiService.submitIzinBulk(
//                    jenisId = jenisIdBody,
//                    kategId = katIdBody,
//                    tglList = tglBodies,
//                    catatan = catatanBody,
//                    file = filePart,
//                    jam = jamBody
//                )


//                if (successCount == dateList.size) {
//                    isSuccess = true
//                    submitMessage = "Berhasil mengajukan izin untuk $successCount hari."
//                } else {
//                    submitMessage = "Selesai. Berhasil: $successCount, Gagal: ${dateList.size - successCount}."
//                }
//                if (response.success) {
//                    isSuccess = true // PENTING: Ini memicu navigasi kembali
//                    submitMessage = "Berhasil mengajukan izin untuk ${dateList.size} hari."
//                } else {
//                    isSuccess = false
//                    submitMessage = "Gagal: ${response.message}"
//                }
            } catch (e: Exception) {
                submitMessage = "Terjadi kesalahan sistem: ${e.message}"
            } finally {
                isSubmitting = false
            }
        }
    }

    private fun getDatesBetween(startMillis: Long, endMillis: Long): List<String> {
        val dates = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startMillis

        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = endMillis

        while (!calendar.after(endCalendar)) {
            dates.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
        return dates
    }
}

class IzinAddViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IzinAddViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IzinAddViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}