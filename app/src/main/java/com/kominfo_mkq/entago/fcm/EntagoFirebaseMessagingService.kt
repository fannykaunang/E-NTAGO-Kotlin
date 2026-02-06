package com.kominfo_mkq.entago.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kominfo_mkq.entago.MainActivity
import com.kominfo_mkq.entago.R

class EntagoFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 1. Ambil data dari payload (objek Data di C#)
        val notifId = remoteMessage.data["notification_id"]
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        android.util.Log.d("NOTIF_DEBUG", "Pesan Masuk - ID: $notifId, Title: $title")
        // 2. Kirim ke fungsi showNotification
        showNotification(title, body, notifId)
    }

    private fun showNotification(title: String?, message: String?, notifId: String?) {
        // 1. Buat Intent dengan Action yang terdaftar di Manifest
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_SHOW_NOTIFICATION" // Harus sama dengan Manifest
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP

            // Bungkus data ke dalam Bundle agar lebih aman saat melewati sistem Android
            val bundle = Bundle().apply {
                putString("TARGET_NOTIF_ID", notifId)
            }
            putExtras(bundle)
        }

        // 2. RequestCode unik (sangat penting agar data tidak tertukar/null)
        val requestCode = notifId?.toIntOrNull() ?: System.currentTimeMillis().toInt()

        // 3. Gunakan FLAG_MUTABLE untuk Android 12+ (API 31+)
        // agar data Extra bisa dibaca oleh Activity yang sudah aktif
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, "entago_broadcast_channel")
            .setSmallIcon(R.drawable.logo_entago)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Pastikan Channel ID dibuat jika belum ada
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "entago_broadcast_channel",
                "E-NTAGO Notifikasi",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(requestCode, notificationBuilder.build())
    }
}