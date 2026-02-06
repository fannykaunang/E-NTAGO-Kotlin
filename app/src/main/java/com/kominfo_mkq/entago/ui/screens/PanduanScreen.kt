package com.kominfo_mkq.entago.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

// Model Data
data class GuideItem(
    val id: Int,
    val title: String,
    val content: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanduanScreen(
    navController: NavHostController
) {
    val guideList = listOf(
        GuideItem(
            1, "Cara Melakukan Absensi",
            "1. Pastikan GPS aktif.\n2. Berada dalam radius kantor.\n3. Tekan Presensi Masuk/Presensi Pulang sesuai jam kantor.\n4. Lakukan verifikasi jika diminta.",
            Icons.Default.LocationOn
        ),
        GuideItem(
            2, "Tugas Luar",
            "Menu ini digunakan jika Anda bekerja di luar kantor dinas.\n\n" +
                    "• Klik 'Tugas Luar' pada Menu Utama.\n" +
                    "• Unggah Foto Surat Tugas atau Foto Kegiatan.\n" +
                    "• Sistem akan mencatat lokasi koordinat Anda saat itu sebagai bukti kehadiran di lokasi penugasan.",
            Icons.Default.Badge
        ),
        GuideItem(
            3, "Pengajuan Izin & Cuti",
            "Gunakan menu ini jika Anda tidak bisa hadir karena alasan sah.\n\n" +
                    "• Izin Sakit: Wajib mengunggah Surat Keterangan Dokter.\n" +
                    "• Cuti Tahunan: Pastikan sisa kuota cuti Anda mencukupi.\n" +
                    "• Status pengajuan dapat dipantau langsung pada riwayat kehadiran.",
            Icons.AutoMirrored.Filled.EventNote
        ),
        GuideItem(
            4, "Lokasi Kantor Saya",
            "Menu ini menampilkan titik koordinat resmi kantor Anda.\n\n" +
                    "• Jika posisi Anda berada di luar lingkaran radius, tombol absen tidak akan aktif.\n" +
                    "• Periksa jarak Anda dengan titik pusat kantor melalui peta yang tersedia di menu ini.",
            Icons.Default.Store
        ),
        GuideItem(
            5, "Peta Sebaran Kehadiran",
            "Menampilkan visualisasi lokasi absensi Anda.\n\n" +
                    "• Titik Biru: Absen tepat di lokasi kantor.\n" +
                    "• Titik Kuning: Absen Tugas Luar.\n" +
                    "• Fitur ini membantu Anda memverifikasi keakuratan koordinat yang terbaca oleh sistem.",
            Icons.Default.Map
        ),
        GuideItem(
            6, "Rekapitulasi Bulanan",
            "Ringkasan statistik kehadiran Anda dalam satu bulan.\n\n" +
                    "• Persentase Kehadiran: Persentase Kehadiran dibagi Jumlah Hari Kerja dalam bulan ini.\n" +
                    "• Hadir: Total Kehadiran dalam bulan ini.\n" +
                    "• Total Jam: Akumulasi Jam Kerja dalam bulan ini.\n" +
                    "• Izin: Total melakukan Izin dalam bulan ini.\n" +
                    "• Alpa/Tanpa Keterangan: Hari tanpa catatan absensi/izin.",
            Icons.Default.Analytics
        ),
        GuideItem(
            7, "Sinkronisasi Data Offline",
            "• Saat blank spot, data tersimpan di HP. HP akan otomatis melakukan sinkronisasi ke server.\n" +
            "• Segera klik 'Sinkronisasi Manual' di menu Pengaturan setelah mendapat sinyal internet.",
            Icons.Default.CloudOff
        ),
        GuideItem(
            9, "Keamanan Akun",
            "• Ubah Kata sandi: Ubah Kata sandi Akun E-NTAGO Anda.\n" +
                    "• Autentikasi Biometrik: Aktifkan Biometrik (Sidik Jari) untuk login lebih cepat dan aman tanpa Email dan Kata sandi.",
            Icons.Default.LockReset
        ),
        GuideItem(
            10, "FAQ: Masalah Umum",
            "T: Kenapa lokasi tidak akurat?\nJ: Buka Google Maps sebentar untuk memperbarui posisi GPS Anda sebelum membuka E-NTAGO.",
            Icons.Default.QuestionAnswer
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panduan Pengguna", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            InfoBanner()
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
            ) {
                items(guideList, key = { it.id }) { item ->
                    ExpandableGuideCard(item)
                }
            }
        }
    }
}

@Composable
fun InfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.Help, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Klik topik untuk detail panduan fitur E-NTAGO Merauke.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ExpandableGuideCard(item: GuideItem) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "")

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(300, easing = LinearOutSlowInEasing)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.rotate(rotationState)) {
                    Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (expanded) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp)
                )
            }
        }
    }
}