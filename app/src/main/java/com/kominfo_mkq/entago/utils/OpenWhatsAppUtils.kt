package com.kominfo_mkq.entago.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

fun openWhatsApp(context: Context, nama: String, skpd: String, deviceId: String) {
    //val phoneNumber = "6282141561944" Nomor Bidang
    val phoneNumber = "6285190079454"

    val message = """
    *HALO TIM IT KOMINFO MERAUKE*
    Saya butuh bantuan terkait aplikasi E-NTAGO:
    
    *Nama:* $nama
    *SKPD:* $skpd
    
    *Device ID:* $deviceId
    
    *Kendala saya:* (Tuliskan kendala Anda di sini...)
""".trimIndent()

    try {
        // Encode pesan agar aman untuk URL
        val url = "https://wa.me/$phoneNumber?text=${URLEncoder.encode(message, "UTF-8")}"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "WhatsApp tidak terpasang di perangkat Anda", Toast.LENGTH_SHORT).show()
    }
}