package com.kominfo_mkq.entago.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.Color
import android.graphics.Paint
import java.util.Date

object FileUtils {

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File.createTempFile("upload_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)

        // 1. Tentukan batas maksimal ukuran file (1MB agar aman untuk batas server 3MB)
        val MAX_SIZE = 1000000 // 1MB dalam bytes

        var compressQuality = 100
        var streamLength: Int

        // Loop kompresi
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size

            // Jika ukuran masih > 1MB, turunkan kualitas 5%
            if (streamLength > MAX_SIZE) {
                compressQuality -= 5
            }
        } while (streamLength > MAX_SIZE && compressQuality > 5)

        // Simpan hasil kompresi ke file asli (menimpa file temp)
        try {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val fos = FileOutputStream(file)
            fos.write(bmpStream.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }

    fun addWatermarkAndReduce(
        file: File,
        nama: String,
        nip: String,
        lat: String,
        lng: String
    ): File {
        // 1. Decode file ke Bitmap
        val options = BitmapFactory.Options()
        var bitmap = BitmapFactory.decodeFile(file.path, options)

        // 2. Siapkan Mutable Bitmap & Canvas
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        // 3. Siapkan format teks
        val timeStamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID")).format(Date())
        val watermarkText = """
            Nama: $nama
            NIP: $nip
            Lokasi: $lat, $lng
            Waktu: $timeStamp
        """.trimIndent()

        // 4. Atur Paint untuk Teks
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = result.width / 40f // Ukuran font proporsional dengan lebar gambar
            isAntiAlias = true
            style = Paint.Style.FILL
            setShadowLayer(2f, 1f, 1f, Color.BLACK) // Shadow agar terbaca di background terang
        }

        // 5. Gambar background hitam transparan di bawah (agar teks terbaca)
        val lines = watermarkText.split("\n")
        val paintRect = Paint().apply {
            color = Color.BLACK
            alpha = 100 // Transparansi (0-255)
        }

        // Hitung area background berdasarkan jumlah baris
        val margin = 40f
        val lineSpacing = 10f
        val rectHeight = (lines.size * (paint.textSize + lineSpacing)) + (margin * 2)
        canvas.drawRect(0f, result.height - rectHeight, result.width.toFloat(), result.height.toFloat(), paintRect)

        // 6. Gambar tiap baris teks
        var yPos = result.height - rectHeight + margin + paint.textSize
        for (line in lines) {
            canvas.drawText(line, margin, yPos, paint)
            yPos += paint.textSize + lineSpacing
        }

        // 7. Kompresi dan simpan kembali ke file
        return saveBitmapToFile(result, file)
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File): File {
        var compressQuality = 100
        var streamLength: Int
        val MAX_SIZE = 1000000 // 1MB

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            streamLength = bmpStream.toByteArray().size
            if (streamLength > MAX_SIZE) compressQuality -= 5
        } while (streamLength > MAX_SIZE && compressQuality > 5)

        val fos = FileOutputStream(file)
        val finalStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, finalStream)
        fos.write(finalStream.toByteArray())
        fos.flush()
        fos.close()
        return file
    }

    fun getFileFromUriIzin(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)

        // Buat file temporary
        val tempFile = File.createTempFile("upload_", getExtension(type), context.cacheDir)

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri) ?: return null

            // LOGIKA KOMPRESI GAMBAR
            if (type?.startsWith("image/") == true) {
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = FileOutputStream(tempFile)
                // Compress 75%
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                outputStream.flush()
                outputStream.close()
            } else {
                // Jika PDF/Lainnya, salin biasa
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                outputStream.close()
            }
            inputStream?.close()
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getExtension(mimeType: String?): String {
        return when (mimeType) {
            "application/pdf" -> ".pdf"
            "image/png" -> ".png"
            "image/jpeg" -> ".jpg"
            else -> ".tmp"
        }
    }
}