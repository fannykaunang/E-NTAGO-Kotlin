package com.kominfo_mkq.entago.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.data.remote.response.IzinDetailData
import com.kominfo_mkq.entago.ui.theme.StatusApproved
import com.kominfo_mkq.entago.ui.theme.StatusPending
import com.kominfo_mkq.entago.ui.theme.StatusRejected
import com.kominfo_mkq.entago.ui.viewmodel.IzinDetailUiState
import com.kominfo_mkq.entago.ui.viewmodel.IzinDetailViewModel
import com.kominfo_mkq.entago.utils.TimeUtils.formatDate
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IzinDetailScreen(
    urutan: Long,
    navController: NavHostController,
    viewModel: IzinDetailViewModel
) {
    val uiState = viewModel.uiState

    LaunchedEffect(urutan) {
        viewModel.fetchDetail(urutan)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pengajuan", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is IzinDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is IzinDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.fetchDetail(urutan) }) { Text("Coba Lagi") }
                    }
                }
                is IzinDetailUiState.Success -> {
                    DetailContent(state.data)
                }
            }
        }
    }
}

@Composable
fun DetailContent(item: IzinDetailData) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Helper Formatter

    // Status Config
    val (statusColor, statusIcon, statusLabel) = when (item.izin_Status) {
        1 -> Triple(StatusApproved, Icons.Default.CheckCircle, "Disetujui")
        0 -> Triple(StatusRejected, Icons.Default.Cancel, "Ditolak")
        2 -> Triple(StatusPending, Icons.Default.HourglassEmpty, "Menunggu Review")
        else -> Triple(Color.Gray, Icons.AutoMirrored.Filled.Help, "Proses")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. STATUS HEADER ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = statusColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(statusIcon, null, tint = Color.White, modifier = Modifier.size(42.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Status Pengajuan", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    Text(statusLabel, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (!item.ket_Status.isNullOrEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Note: ${item.ket_Status}", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    }
                }
            }
        }

        // --- 2. INFORMASI IZIN ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Informasi Izin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))

                DetailRow(Icons.Default.Category, "Jenis Izin", item.izin_Jenis_Name)
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                DetailRow(Icons.AutoMirrored.Filled.Label, "Kategori", item.kat_Izin_Nama)
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (item.izin_Jenis_Id in listOf(73, 100, 101)) {
                    DetailRow(Icons.Default.AccessTime, "Waktu", item.izin_No_Scan_Time)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                DetailRow(Icons.Default.History, "Diajukan Pada", formatDate(item.izin_Tgl_Pengajuan))
            }
        }

        // --- 3. RENTANG WAKTU ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Waktu Pelaksanaan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mulai
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Mulai", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatDate(item.izin_Tgl_Mulai), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Panah
                    Icon(Icons.AutoMirrored.Filled.ArrowRightAlt, null, tint = MaterialTheme.colorScheme.primary)

                    // Selesai
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Selesai", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatDate(item.izin_Tgl_Selesai), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // --- 4. CATATAN ---
        if (!item.izin_Catatan.isNullOrEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Notes, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Catatan / Keterangan", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.izin_Catatan,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // --- 5. FILE LAMPIRAN ---
        if (!item.file_Path.isNullOrEmpty()) {
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(item.file_Path)
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AttachFile, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Lihat Dokumen Lampiran", fontWeight = FontWeight.Bold)
            }
        }

        // Spacer Bawah
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}