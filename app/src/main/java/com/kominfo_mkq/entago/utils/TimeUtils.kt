package com.kominfo_mkq.entago.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun formatTimeAgo(dateString: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        // Pastikan timezone sesuai dengan server (WIT untuk Merauke)
        sdf.timeZone = TimeZone.getTimeZone("GMT+9")

        return try {
            val pastDate = sdf.parse(dateString) ?: return dateString
            val now = Date()
            val seconds = (now.time - pastDate.time) / 1000

            when {
                seconds < 60 -> "Baru saja"
                seconds < 3600 -> "${seconds / 60} menit yang lalu"
                seconds < 86400 -> "${seconds / 3600} jam yang lalu"
                seconds < 604800 -> "${seconds / 86400} hari yang lalu"
                seconds < 2592000 -> "${seconds / 604800} minggu yang lalu"
                else -> {
                    // Jika sudah lebih dari sebulan, tampilkan tanggal aslinya
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                    outputFormat.format(pastDate)
                }
            }
        } catch (_: Exception) {
            dateString // Kembalikan string asli jika gagal parse
        }
    }

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "-"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val date = inputFormat.parse(dateString) ?: return dateString
            outputFormat.format(date)
        } catch (_: Exception) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val date = inputFormat.parse(dateString) ?: return dateString
                outputFormat.format(date)
            } catch (_: Exception) {
                dateString
            }
        }
    }

    fun formatDateWithTime(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "-"
        return try {
            // Input format: Sesuaikan dengan format dari API/Server
            // Disarankan pakai Locale.US untuk input parser agar stabil di semua device
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

            // Output format: "dd MMMM yyyy HH:mm"
            // Contoh hasil: "06 Februari 2026 13:08"
            val outputFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))

            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (_: Exception) {
            dateString // Kembalikan string asli jika error parsing
        }
    }
}