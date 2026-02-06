package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kominfo_mkq.entago.data.local.dao.TugasLuarDao
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.request.ChangePasswordRequest
import com.kominfo_mkq.entago.data.remote.response.BaseResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SettingsViewModel(private val apiService: ApiService) : ViewModel() {

    // State untuk memantau proses
    var isLoading by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    var isSuccess by mutableStateOf(false)
        private set

    fun updatePassword(oldPass: String, newPass: String, onComplete: (Boolean) -> Unit) {
        // Validasi client-side sederhana
        if (oldPass.isEmpty() || newPass.isEmpty()) {
            message = "Semua kolom wajib diisi."
            return
        }

        if (newPass.length < 6) {
            message = "Password baru minimal 6 karakter."
            return
        }

        viewModelScope.launch {
            isLoading = true
            message = "" // Reset pesan
            try {
                val response = apiService.changePassword(ChangePasswordRequest(oldPass, newPass))

                if (response.isSuccessful) {
                    // Status 200 OK
                    message = response.body()?.message ?: "Password berhasil diperbarui."
                    isSuccess = true
                    onComplete(true)
                } else {
                    // Menangkap error 400, 401, atau 500 dari backend
                    val errorJson = response.errorBody()?.string()
                    val errorObj = Gson().fromJson(errorJson, BaseResponse::class.java)
                    message = errorObj?.message ?: "Gagal memperbarui password."
                    isSuccess = false
                    onComplete(false)
                }
            } catch (_: Exception) {
                message = "Terjadi kesalahan jaringan. Coba lagi nanti."
                isSuccess = false
                onComplete(false)
            } finally {
                isLoading = false
            }
        }
    }

    fun syncManualTugasLuar(dao: TugasLuarDao) {
        viewModelScope.launch {
            isLoading = true
            message = "Menghubungkan ke server..."

            try {
                val drafts = dao.getAllDrafts()
                if (drafts.isEmpty()) {
                    message = "Tidak ada data offline."
                    isLoading = false
                    return@launch
                }

                var successCount = 0

                drafts.forEach { draft ->
                    val file = File(draft.imagePath)
                    if (!file.exists()) {
                        dao.deleteDraft(draft)
                        return@forEach
                    }

                    // Menyiapkan data untuk ApiService (@Part)
                    val tujuanBody = draft.tujuan.toRequestBody("text/plain".toMediaTypeOrNull())
                    val keteranganBody = draft.keterangan_tugas.toRequestBody("text/plain".toMediaTypeOrNull())
                    val alamatBody = draft.alamat.toRequestBody("text/plain".toMediaTypeOrNull())
                    val latBody = draft.latitude.toRequestBody("text/plain".toMediaTypeOrNull())
                    val lngBody = draft.longitude.toRequestBody("text/plain".toMediaTypeOrNull())

                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)

                    val response = apiService.uploadTugasLuar(
                        tujuanBody,
                        keteranganBody, // Mengirim ke parameter @Part("keterangan_tugas")
                        alamatBody,
                        latBody,
                        lngBody,
                        fotoPart
                    )

                    if (response.success) {
                        dao.deleteDraft(draft)

                        if (file.exists()) {
                            val deleted = file.delete()
                            if (deleted) {
                                android.util.Log.d("SYNC_DEBUG", "File fisik berhasil dihapus: ${file.name}")
                            }
                        }

                        successCount++
                    }
                }

                message = "$successCount laporan berhasil tersinkronisasi."
            } catch (e: Exception) {
                message = "Gagal sinkron: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearMessage() {
        message = ""
        isSuccess = false
    }

    fun clearPhotoCache(context: android.content.Context, dao: TugasLuarDao) {
        viewModelScope.launch {
            isLoading = true
            message = "Memindai file cache..."

            try {
                // 1. Ambil semua path foto yang masih ada di database (Draft)
                val activeDrafts = dao.getAllDrafts()
                val activePaths = activeDrafts.map { it.imagePath }

                // 2. Akses folder cache
                val cacheDir = context.cacheDir
                val files = cacheDir.listFiles()

                var deletedCount = 0
                var spaceSaved = 0L

                if (files != null) {
                    for (file in files) {
                        // Cek apakah file adalah foto tugas luar (berdasarkan prefix "tugas_")
                        // dan pastikan file tersebut TIDAK ada di daftar database aktif
                        if (file.name.startsWith("tugas_") && !activePaths.contains(file.absolutePath)) {
                            spaceSaved += file.length()
                            if (file.delete()) {
                                deletedCount++
                            }
                        }
                    }
                }

                // Hitung ukuran memori yang dibersihkan dalam MB
                val mbSaved = spaceSaved / (1024 * 1024)
                message = if (deletedCount > 0) {
                    "Berhasil menghapus $deletedCount file sampah ($mbSaved MB)."
                } else {
                    "Cache sudah bersih. Tidak ada file sampah ditemukan."
                }

            } catch (e: Exception) {
                message = "Gagal membersihkan cache: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}

// Factory agar bisa menerima ApiService
class SettingsViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(apiService) as T
    }
}