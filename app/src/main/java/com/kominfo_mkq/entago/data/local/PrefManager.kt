package com.kominfo_mkq.entago.data.local

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kominfo_mkq.entago.data.remote.response.UserData

class PrefManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "e_ntago_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthData(token: String, user: UserData) {
        sharedPrefs.edit().apply {
            putString("token", token)
            putInt("userid", user.userid)
            putString("email", user.email)
            putString("pin", user.pin)
            putInt("skpdid", user.skpdid)
            putInt("level", user.level)
            putString("deviceid", user.deviceid)
            putString("latitude", user.latitude)
            putString("longitude", user.longitude)
            apply()
        }
    }

    private val THEME_KEY = "is_dark_mode"

    fun setDarkMode(isDark: Boolean) {
        sharedPrefs.edit { putBoolean(THEME_KEY, isDark) }
    }

    fun isDarkMode(): Boolean {
        // Default false (Light Mode) jika data belum pernah disimpan
        return sharedPrefs.getBoolean(THEME_KEY, false)
    }

    fun getSn(): String? {
        return sharedPrefs.getString("sn", null)
    }

    fun getDeviceid(): String? {
        return sharedPrefs.getString("deviceid", null)
    }

    fun getPegawaiId(): Int {
        return sharedPrefs.getInt("pegawai_id", 0)
    }

    fun getPin(): String? {
        return sharedPrefs.getString("pin", null)
    }

//    fun isLoggedIn(): Boolean {
//        // Jika token tidak null dan tidak kosong, berarti user sudah login
//        return !sharedPrefs.getString("token", null).isNullOrEmpty()
//    }

    // Di PrefManager.kt
    fun savePegawaiProfile(nama: String, nip: String, peg_id: Int, lat: String?, lng: String?, sn: String?, deviceId: String?, skpd: String) {
        sharedPrefs.edit {
            putString("pegawai_nama", nama)
            putString("pegawai_nip", nip)
            putInt("pegawai_id", peg_id)
            // Simpan koordinat dari response getPegawai
            putString("latitude", lat)
            putString("longitude", lng)
            putString("sn", sn)
            putString("deviceid", deviceId)
            putString("skpd", skpd)
        }
    }

    fun getNama(): String {
        return sharedPrefs.getString("pegawai_nama", "") ?: ""
    }

    fun getSkpd(): String {
        return sharedPrefs.getString("skpd", "") ?: ""
    }

    fun getNip(): String {
        return sharedPrefs.getString("pegawai_nip", "") ?: ""
    }

    fun getSkpdid(): Int = sharedPrefs.getInt("skpdid", 0)

    fun getToken(): String? = sharedPrefs.getString("token", null)

    fun getLatitude(): Double {
        val lat = sharedPrefs.getString("latitude", "-8.488")
        // Cek jika string kosong atau null, kembalikan default
        return if (lat.isNullOrBlank()) -8.488 else lat.toDouble()
    }

    // Di PrefManager.kt
    fun getLongitude(): Double {
        val lat = sharedPrefs.getString("longitude", "140.399")
        // Cek jika string kosong atau null, kembalikan default
        return if (lat.isNullOrBlank()) 140.399 else lat.toDouble()
    }

//    // Fungsi untuk Logout (Hapus data)
//    fun clearData() {
//        sharedPrefs.edit { clear() }
//    }

    // Di dalam PrefManager.kt
    fun saveBiometricCredentials(email: String, pass: String) {
        sharedPrefs.edit { putString("bio_email", email) }
        sharedPrefs.edit { putString("bio_pass", pass) }
    }

    fun getBiometricEmail(): String? = sharedPrefs.getString("bio_email", null)
    fun getBiometricPass(): String? = sharedPrefs.getString("bio_pass", null)

    // Di dalam PrefManager.kt

    fun logout() {
        sharedPrefs.edit().apply {
            // PERBAIKAN: Gunakan "token" sesuai dengan yang ada di saveAuthData
            remove("token")
            remove("pegawai_nama") // Sesuaikan dengan key di savePegawaiProfile
            remove("pegawai_nip")
            apply()
        }
    }

    private val BIOMETRIC_ENABLED_KEY = "biometric_enabled"

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPrefs.edit { putBoolean(BIOMETRIC_ENABLED_KEY, enabled) }
    }

    fun isBiometricEnabled(): Boolean {
        // Default true, agar fitur langsung aktif setelah login pertama
        return sharedPrefs.getBoolean(BIOMETRIC_ENABLED_KEY, true)
    }

    fun updateLocalDeviceId(newId: String) {
        sharedPrefs.edit { putString("deviceid", newId) }
    }
}