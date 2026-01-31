package com.kominfo_mkq.entago.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.response.TugasLuarData
import com.kominfo_mkq.entago.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class TugasLuarEditViewModel(private val apiService: ApiService) : ViewModel() {
    var id by mutableIntStateOf(0)
    var tujuan by mutableStateOf("")
    var keterangan by mutableStateOf("")
    var alamat by mutableStateOf("")
    var latitude by mutableStateOf("")
    var longitude by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null) // URI foto baru jika diganti
    var oldImageUrl by mutableStateOf("") // URL foto lama untuk preview

    var isLoading by mutableStateOf(false)
    var updateStatus by mutableStateOf<String?>(null)

    // Fungsi untuk mengisi form dengan data lama
    fun initData(tugas: TugasLuarData, baseUrl: String) {
        id = tugas.id
        tujuan = tugas.tujuan
        keterangan = tugas.keterangan
        alamat = tugas.alamat
        latitude = tugas.latitude
        longitude = tugas.longitude
        oldImageUrl = "$baseUrl/${tugas.lampiranPath?.trimStart('/')}"
    }

    fun updateTugas(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    val tujuanPart = tujuan.toRequestBody("text/plain".toMediaTypeOrNull())
                    val ketPart = keterangan.toRequestBody("text/plain".toMediaTypeOrNull())
                    val alamatPart = alamat.toRequestBody("text/plain".toMediaTypeOrNull())
                    val latPart = latitude.toRequestBody("text/plain".toMediaTypeOrNull())
                    val lngPart = longitude.toRequestBody("text/plain".toMediaTypeOrNull())

                    var imagePart: MultipartBody.Part? = null
                    if (imageUri != null) {
                        var file = FileUtils.getFileFromUri(context, imageUri!!)
                        if (file != null) {
                            file = FileUtils.reduceFileImage(file)
                            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                            imagePart = MultipartBody.Part.createFormData("foto", file.name, requestFile)
                        }
                    }

                    val response = apiService.updateTugasLuar(id, tujuanPart, ketPart, alamatPart, latPart, lngPart, imagePart)

                    withContext(Dispatchers.Main) {
                        // SEKARANG atribut success dan message pasti terbaca
                        if (response.success) {
                            updateStatus = "Berhasil diperbarui"
                            onSuccess()
                        } else {
                            updateStatus = response.message
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateStatus = "Gagal: ${e.message}"
                }
            } finally {
                isLoading = false
            }
        }
    }
}

class TugasLuarEditViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TugasLuarEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TugasLuarEditViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}