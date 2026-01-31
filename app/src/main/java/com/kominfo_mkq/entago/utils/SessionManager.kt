package com.kominfo_mkq.entago.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {
    // Menggunakan Unit karena kita hanya perlu tahu 'kapan' kejadian itu terjadi
    // replay = 0 agar sinyal tidak muncul lagi saat user buka aplikasi setelah logout
    // extraBufferCapacity = 1 agar sinyal tidak hilang jika collector sedang sibuk
    private val _unauthorizedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    suspend fun triggerUnauthorized() {
        _unauthorizedEvent.emit(Unit)
    }
}