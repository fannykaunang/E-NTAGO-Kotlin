package com.kominfo_mkq.entago.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

@SuppressLint("HardwareIds")
fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: "unknown"
}

fun formatPeriode(periode: String?): String {
    if (periode.isNullOrBlank()) return "-"

    return try {
        val parts = periode.split("-")
        val tahun = parts[0]
        val bulan = parts[1]

        val namaBulan = when (bulan) {
            "01" -> "Januari"
            "02" -> "Februari"
            "03" -> "Maret"
            "04" -> "April"
            "05" -> "Mei"
            "06" -> "Juni"
            "07" -> "Juli"
            "08" -> "Agustus"
            "09" -> "September"
            "10" -> "Oktober"
            "11" -> "November"
            "12" -> "Desember"
            else -> bulan
        }

        "$namaBulan $tahun"
    } catch (e: Exception) {
        periode // Kembalikan teks asli jika gagal parsing
    }
}