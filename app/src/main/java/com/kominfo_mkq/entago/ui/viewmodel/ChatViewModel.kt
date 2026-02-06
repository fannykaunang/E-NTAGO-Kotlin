package com.kominfo_mkq.entago.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.kominfo_mkq.entago.BuildConfig
import com.kominfo_mkq.entago.data.model.ChatMessage
import com.kominfo_mkq.entago.data.remote.ApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ChatViewModel(
    private val apiService: ApiService
) : ViewModel() {
    private var userContextData: String = "Data belum tersedia."
    // List pesan yang tampil di UI
    // Menggunakan mutableStateListOf agar otomatis update UI saat ada pesan baru
    val messages = mutableStateListOf<ChatMessage>()

    // Inisialisasi Gemini AI
    private val generativeModel = GenerativeModel(
        //modelName = "gemini-3-flash-preview",
        modelName = "gemini-2.5-flash-lite", // Model cepat dan hemat
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // Memulai sesi chat dengan "System Instruction" (Kepribadian)
    // Kita menyuntikkan konteks di awal history agar AI paham tugasnya
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("Halo, siapa kamu?") },
            content(role = "model") { text("Namek, Namuk. Entago! Saya adalah Asisten Virtual AI E-NTAGO. Saya siap membantu Anda terkait absensi, pengajuan izin, dan informasi kepegawaian di lingkungan Pemkab Merauke. Ada yang bisa saya bantu?") }
        )
    )

    init {
        // OTOMATIS LOAD DATA SAAT HALAMAN DIBUKA
        fetchUserContextData()
    }

    fun sendMessage(userText: String) {
        // 1. Tambahkan pesan User ke layar
        messages.add(ChatMessage(text = userText, isFromUser = true))

        // 2. Tambahkan indikator "Sedang mengetik..."
        val loadingId = "loading_indicator"
        messages.add(ChatMessage(id = loadingId, text = "...", isFromUser = false, isTyping = true))

        viewModelScope.launch {
            try {
                // 3. Kirim ke AI dengan Prompt Tambahan (Context Injection)
                // Kita beri sedikit konteks agar AI tetap fokus
                val prompt = """
                    Konteks: Kamu adalah asisten aplikasi absensi pegawai e-ntago. Jawablah dengan sopan, formal, dan singkat.
                    Pertanyaan Pegawai: $userText
                """.trimIndent()

                val response = chat.sendMessage(prompt)

                // 4. Hapus loading, tampilkan jawaban
                messages.removeIf { it.id == loadingId }

                response.text?.let { responseText ->
                    messages.add(ChatMessage(text = responseText, isFromUser = false))
                }

            } catch (e: Exception) {
                delay(500)
                messages.removeIf { it.id == loadingId }

                // Tampilkan pesan yang ramah kepada user
                messages.add(
                    ChatMessage(
                        text = "Mohon maaf ada kesalahan koneksi. Silahkan ulangi lagi pertanyaan Anda.",
                        isFromUser = false
                    )
                )

                // Tetap log ke Logcat agar Anda sebagai developer bisa pantau jika ada error
                android.util.Log.e("AI_ERROR", "Error: ${e.message}")
            }
        }
    }

//    fun sendSmartMessage(userText: String) {
//        messages.add(ChatMessage(text = userText, isFromUser = true))
//        val loadingId = "loading_indicator"
//        messages.add(ChatMessage(id = loadingId, text = "...", isFromUser = false, isTyping = true))
//
//        viewModelScope.launch {
//            try {
//                // --- PERBAIKAN DI SINI ---
//                // Gabungkan Konteks Data Pegawai dengan Pertanyaan User
//                val smartPrompt = """
//                    KONTEKS DATA PEGAWAI:
//                    $userContextData
//
//                    INSTRUKSI:
//                    1. Kamu adalah asisten resmi aplikasi E-NTAGO Pemkab Merauke.
//                    2. Jawab pertanyaan berdasarkan DATA PEGAWAI di atas.
//                    3. Jika ditanya nama atau kehadiran, gunakan data tersebut.
//                    4. Jawab dengan sopan, formal, dan singkat.
//
//                    PERTANYAAN USER: $userText
//                """.trimIndent()
//
//                val response = chat.sendMessage(smartPrompt)
//
//                messages.removeIf { it.id == loadingId }
//                response.text?.let { responseText ->
//                    messages.add(ChatMessage(text = responseText, isFromUser = false))
//                }
//
//            } catch (e: Exception) {
//                delay(500) // Impor kotlinx.coroutines.delay
//                messages.removeIf { it.id == loadingId }
//
//                // Tampilkan pesan yang ramah kepada user
//                messages.add(
//                    ChatMessage(
//                        text = "Mohon maaf terjadi kesalahan. Silahkan ulangi lagi pertanyaan Anda.",
//                        isFromUser = false
//                    )
//                )
//
//                // Tetap log ke Logcat agar Anda sebagai developer bisa pantau jika ada error
//                android.util.Log.e("AI_ERROR", "Error: ${e.message}")
//            }
//        }
//    }

    private fun fetchUserContextData() {
        viewModelScope.launch {
            try {
                // 1. Ambil Waktu Saat Ini
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH) + 1 // Januari = 1, dst.

                // Format untuk mencocokkan dengan "periode_Bulan": "2026-01"
                val currentPeriodTarget = String.format(Locale.US, "%d-%02d", currentYear, currentMonth)

                // 2. Panggil API (Tahun dinamis sesuai waktu sekarang)
                val response = apiService.getRekapBulanan(
                    year = currentYear,
                    excludeWeekend = true
                )

                // 3. Proses Data Jika Sukses
                if (response.success) {
                    val listRekap = response.data

                    val data = response.data.firstOrNull()

                    if (data != null) {
                        // DI SINI TEMPATNYA:
                        userContextData = """
                        DATA PEGAWAI:
                        - Nama: ${data.pegawai_Nama}
                        - Status: Pegawai Aktif
                        - Statistik Bulan ${data.periode_Bulan}:
                          * Hadir: ${data.hadir} hari
                          * Izin: ${data.izin} hari
                          * Alpa: ${data.alpa} hari
                          * Jam Kerja: ${data.total_Jam_Kerja} jam
                    """.trimIndent()
                    }

                    // Cari data yang sesuai dengan bulan sekarang
                    val dataBulanIni = listRekap.find { it.periode_Bulan == currentPeriodTarget }

                    val contextBuilder = StringBuilder()
                    contextBuilder.append("Informasi Rekapitulasi Pegawai:\n")

                    if (dataBulanIni != null) {
                        contextBuilder.append("""
                        Data Bulan Ini (${dataBulanIni.periode_Bulan}):
                        - Nama Pegawai: ${dataBulanIni.pegawai_Nama}
                        - Hari Kerja: ${dataBulanIni.total_Hari_Kerja} hari
                        - Hadir: ${dataBulanIni.hadir} hari
                        - Izin: ${dataBulanIni.izin} hari
                        - Alpa: ${dataBulanIni.alpa} hari
                        - Total Jam Kerja: ${dataBulanIni.total_Jam_Kerja} jam
                        - Persentase Kehadiran: ${dataBulanIni.persentase_Kehadiran}%
                        
                    """.trimIndent())
                    } else {
                        contextBuilder.append("- Data untuk bulan $currentPeriodTarget belum tersedia.\n")
                    }

                    // 4. Tambahkan Ringkasan Akumulasi Tahunan (Opsional tapi berguna untuk AI)
                    val akumulasiHadir = listRekap.sumOf { it.hadir }
                    val akumulasiAlpa = listRekap.sumOf { it.alpa }

                    contextBuilder.append("""
                    Ringkasan Akumulasi Tahun $currentYear:
                    - Total Hadir Setahun: $akumulasiHadir hari
                    - Total Alpa Setahun: $akumulasiAlpa hari
                """.trimIndent())

                    // Simpan ke variabel global context
                    userContextData = contextBuilder.toString()

                } else {
                    userContextData = "Gagal memuat data"
                }
            } catch (e: Exception) {
                userContextData = "Maaf, sistem gagal mengambil data rekapitulasi terbaru."
                e.printStackTrace()
            }
        }
    }
}

// Factory sederhana
class ChatViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(apiService) as T
    }
}