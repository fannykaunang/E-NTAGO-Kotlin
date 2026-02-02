package com.kominfo_mkq.entago.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.data.remote.response.NotifDetailData
import com.kominfo_mkq.entago.ui.components.EmptyState
import com.kominfo_mkq.entago.ui.viewmodel.NotifDetailUiState
import com.kominfo_mkq.entago.ui.viewmodel.NotificationDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notifId: String?,
    navController: NavHostController,
    viewModel: NotificationDetailViewModel
) {
    LaunchedEffect(notifId) {
        notifId?.let { viewModel.loadDetail(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Notifikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = viewModel.uiState) {
                is NotifDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is NotifDetailUiState.Error -> {
                    // Mengganti ErrorState dengan EmptyState dari komponen kamu
                    EmptyState(
                        isSearching = false, // Karena ini halaman detail, bukan hasil pencarian
                        title = "Gagal Memuat Detail Notifikasi",
                        description = state.message, // Menampilkan pesan error dari ViewModel
                        icon = androidx.compose.material.icons.Icons.Default.CloudOff
                    )
                }
                is NotifDetailUiState.Success -> NotificationContent(state.data)
            }
        }
    }
}

@Composable
fun NotificationContent(notif: NotifDetailData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Tag Kategori
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = notif.type?.uppercase() ?: "INFO",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Judul
        Text(
            text = notif.title ?: "",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 34.sp
        )

        // Meta Info (Waktu & Pengirim)
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Text(" ${notif.created_At}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Text(" ${notif.created_By}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Isi Pesan
        Text(
            text = notif.body ?: "",
            modifier = Modifier.padding(vertical = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 28.sp
        )

        // Status Baca (Visual saja)
        if (notif.is_Read) {
            Text(
                text = "Dibaca pada: ${notif.read_At}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.width(8.dp))
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}