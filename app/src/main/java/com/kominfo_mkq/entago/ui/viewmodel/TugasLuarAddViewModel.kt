package com.kominfo_mkq.entago.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.kominfo_mkq.entago.data.local.AppDatabase
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.local.SyncTugasLuarWorker
import com.kominfo_mkq.entago.data.local.entity.TugasLuarEntity
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.TugasLuarUploadResponse
import com.kominfo_mkq.entago.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

class TugasLuarAddViewModel(private val apiService: ApiService) : ViewModel() {
    var tujuan by mutableStateOf("")
    var keterangan by mutableStateOf("")
    var alamat by mutableStateOf("")
    var latitude by mutableStateOf("")
    var longitude by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null)

    var isLoading by mutableStateOf(false)
    var uploadStatus by mutableStateOf<String?>(null)

    var accuracy by mutableStateOf(0f)

    fun submitTugas(context: Context, prefManager: PrefManager, onSuccess: () -> Unit) {
        if (imageUri == null) {
            uploadStatus = "Mohon ambil foto bukti terlebih dahulu"
            return
        }

        viewModelScope.launch {
            isLoading = true

            try {
                withContext(Dispatchers.IO) {
                    val tujuanPart = tujuan.toRequestBody("text/plain".toMediaTypeOrNull())
                    val ketPart = keterangan.toRequestBody("text/plain".toMediaTypeOrNull())
                    val alamatPart = alamat.toRequestBody("text/plain".toMediaTypeOrNull())
                    val latPart = latitude.toRequestBody("text/plain".toMediaTypeOrNull())
                    val lngPart = longitude.toRequestBody("text/plain".toMediaTypeOrNull())

                    val namaPegawai = prefManager.getNama() ?: "Nama Tidak Terdaftar"
                    val nipPegawai = prefManager.getNip() ?: "-"

                    var file = FileUtils.getFileFromUri(context, imageUri!!)

                    if (file != null) {
                        file = FileUtils.addWatermarkAndReduce(
                            file = file,
                            nama = namaPegawai,
                            nip = nipPegawai,
                            lat = latitude,
                            lng = longitude
                        )

                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val imagePart = MultipartBody.Part.createFormData("foto", file.name, requestFile)

                        // --- BAGIAN LOGIKA PENYARINGAN ERROR ---
                        try {
                            val response = apiService.uploadTugasLuar(
                                tujuanPart, ketPart, alamatPart, latPart, lngPart, imagePart
                            )

                            if (response.success) {
                                withContext(Dispatchers.Main) {
                                    uploadStatus = "Berhasil Terkirim"
                                    onSuccess()
                                }
                            }
                        } catch (e: Exception) {
                            // 1. Cek apakah ini error dari Server (400, 401, 500, dll)
                            if (e is retrofit2.HttpException) {
                                val errorBody = e.response()?.errorBody()?.string()
                                val errorMessage = try {
                                    // Parsing pesan "message" dari JSON 400 Bad Request Anda
                                    Gson().fromJson(errorBody, TugasLuarUploadResponse::class.java).message
                                } catch (parseError: Exception) {
                                    "Gagal: ${e.message()}"
                                }

                                withContext(Dispatchers.Main) {
                                    uploadStatus = errorMessage // Tampilkan pesan asli dari API
                                }
                            }
                            // 2. Jika error karena koneksi (Timeout/No Internet), baru simpan OFFLINE
                            else if (e is java.io.IOException) {
                                saveToOffline(context, file)
                                withContext(Dispatchers.Main) {
                                    uploadStatus = "Offline: Laporan disimpan sebagai draft"
                                    onSuccess()
                                }
                            }
                            // 3. Error tak terduga lainnya
                            else {
                                withContext(Dispatchers.Main) {
                                    uploadStatus = "Terjadi kesalahan: ${e.message}"
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            uploadStatus = "Gagal memproses gambar"
                        }
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    private suspend fun saveToOffline(context: Context, file: File?) {
        val appContext = context.applicationContext
        val db = AppDatabase.getDatabase(appContext)
        val dao = db.tugasLuarDao()

        val isDuplicate = dao.checkDuplicate(tujuan, keterangan, alamat)

        if (isDuplicate != null) {
            android.util.Log.d("OFFLINE_DB", "Data sudah ada, membatalkan simpan ganda.")
            return
        }

        val draft = TugasLuarEntity(
            tujuan = tujuan,
            keterangan_tugas = keterangan,
            alamat = alamat,
            latitude = latitude,
            longitude = longitude,
            imagePath = file?.absolutePath ?: "" // Simpan lokasi file di HP
        )

        android.util.Log.d("OFFLINE_DB", "Mencoba simpan draft: ${draft.tujuan}")
        dao.saveDraft(draft)
        android.util.Log.d("OFFLINE_DB", "Draft berhasil disimpan di Room!")
        dao.saveDraft(draft)

        // --- JALANKAN WORKMANAGER DI SINI ---
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Tunggu ada internet
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncTugasLuarWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES) // Coba lagi jika gagal
            .build()

        // Gunakan UniqueWork agar tidak terjadi tumpukan proses sinkronisasi yang sama
        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_tugas_luar",
            ExistingWorkPolicy.APPEND_OR_REPLACE, // Tambahkan ke antrean
            syncRequest
        )
    }

    fun resetStatus() { uploadStatus = null }
}

// Factory tetap sama
class TugasLuarAddViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TugasLuarAddViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TugasLuarAddViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}