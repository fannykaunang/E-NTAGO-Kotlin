package com.kominfo_mkq.entago.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kominfo_mkq.entago.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SyncTugasLuarWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.tugasLuarDao()
        val api = RetrofitClient.getInstance(applicationContext)

        val drafts = dao.getAllDrafts()
        if (drafts.isEmpty()) return Result.success()

        var allSuccess = true

        Log.e("SYNC_WORKER", "Memulai sinkronisasi ${drafts.size} data...")

        for (draft in drafts) {
            // 1. Ambil File Foto dari Internal Storage berdasarkan Path yang tersimpan
            val file = File(draft.imagePath)
            try {

                // Cek apakah file benar-benar ada secara fisik
                if (!file.exists()) {
                    // Jika file hilang (misal: dihapus user), hapus draft dari Room agar tidak stuck selamanya
                    dao.deleteDraft(draft)
                    continue
                }

                // 2. Konversi semua Field teks ke RequestBody
                val tujuanPart = draft.tujuan.toRequestBody("text/plain".toMediaTypeOrNull())
                val keteranganPart = draft.keterangan_tugas.toRequestBody("text/plain".toMediaTypeOrNull())
                val alamatPart = draft.alamat.toRequestBody("text/plain".toMediaTypeOrNull())
                val latitudePart = draft.latitude.toRequestBody("text/plain".toMediaTypeOrNull())
                val longitudePart = draft.longitude.toRequestBody("text/plain".toMediaTypeOrNull())

                // 3. Konversi File Foto ke MultipartBody.Part
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)

                // 4. Eksekusi Upload ke Server
                // Pastikan nama parameter (tujuan, foto, dll) sesuai dengan ApiService Anda
                val response = api.uploadTugasLuar(
                    tujuan = tujuanPart,
                    keterangan = keteranganPart,
                    alamat = alamatPart,
                    latitude = latitudePart,
                    longitude = longitudePart,
                    foto = fotoPart
                )

                // 5. Jika sukses, hapus draft dari database lokal agar tidak dikirim dua kali
                if (response.success) {
                    dao.deleteDraft(draft)
                    // Opsional: Hapus file fisik di HP untuk menghemat ruang penyimpanan
                    if (file.exists()) file.delete()
                } else {
                    allSuccess = false
                }

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 400) {
                    // SERVER MENOLAK (Data duplikat/salah)
                    // Jangan RETRY! Hapus saja dari HP karena dikirim kapanpun akan tetap ditolak.
                    //android.util.Log.e("SYNC_WORKER", "Data ditolak server (400): ${e.message()}")
                    dao.deleteDraft(draft)
                    if (file.exists()) file.delete()
                    allSuccess = false
                } else {
                    // Error lain (misal 500 atau 503), silakan RETRY
                    return Result.retry()
                }
            } catch (_: Exception) {
                // Error jaringan (Koneksi putus), silakan RETRY
                return Result.retry()
            }
        }

        return if (allSuccess) {
            // --- PICU NOTIFIKASI DI SINI ---
            showNotification("Sinkronisasi Berhasil", "Semua laporan tugas luar offline telah terkirim ke server.")
            Result.success()
        } else {
            Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sync_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Sync Data", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done) // Gunakan icon sistem atau icon E-NTAGO
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}