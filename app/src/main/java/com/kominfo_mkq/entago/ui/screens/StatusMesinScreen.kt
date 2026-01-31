package com.kominfo_mkq.entago.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusMesinScreen(
    navController: NavHostController,
    prefManager: PrefManager,
    viewModel: DashboardViewModel
) {
    // 1. Initial Load saat halaman dibuka
    LaunchedEffect(Unit) {
        viewModel.checkDeviceStatus()
    }

    val refreshState = rememberPullToRefreshState()

    // Konfigurasi URL Dinamis
    //val skpdId = prefManager.getSkpdid()
    val skpdRaw = prefManager.getSkpd()
    //val skpdSlug = skpdRaw.lowercase().replace(" ", "-")
    //val dynamicUrl = "https://entago.merauke.go.id/skpd/detail/$skpdId/$skpdSlug"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status Mesin Absen", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        // 2. Bungkus konten dengan PullToRefreshBox
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = viewModel.isLoading, // Terhubung ke state loading di ViewModel
            onRefresh = {
                viewModel.checkDeviceStatus()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // Dibutuhkan agar PullToRefresh bekerja
                    .padding(16.dp)
            ) {
                // Tampilkan Card Status
                DeviceStatusCard(
                    isOnline = viewModel.isMachineOnline,
                    deviceName = "Mesin Absensi $skpdRaw"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tarik ke bawah untuk memperbarui status",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DeviceStatusCard(
    isOnline: Boolean,
    deviceName: String = "Mesin Absensi",
    modifier: Modifier = Modifier
) {
    val statusColor = if (isOnline) Color(0xFF43A047) else Color(0xFFE53935)
    val statusText = if (isOnline) "Online" else "Offline"
    val statusIcon = if (isOnline) Icons.Default.CheckCircleOutline else Icons.Default.Cancel

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(2.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = deviceName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Status: $statusText", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Surface(shape = RoundedCornerShape(10.dp), color = statusColor.copy(alpha = 0.12f), border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = statusText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }
        }
    }
}

//@Composable
//fun CompactDeviceStatus(
//    isOnline: Boolean,
//    modifier: Modifier = Modifier
//) {
//    val statusColor = if (isOnline) Color(0xFF43A047) else Color(0xFFE53935)
//    val statusText = if (isOnline) "Online" else "Offline"
//    val statusIcon = if (isOnline) Icons.Default.CheckCircleOutline else Icons.Default.Cancel
//
//    Card(
//        modifier = modifier.height(130.dp),
//        shape = RoundedCornerShape(20.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.25f))
//    ) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            // Background decoration
//            Box(
//                modifier = Modifier
//                    .size(80.dp)
//                    .offset(x = 50.dp, y = (-15).dp)
//                    .background(
//                        brush = Brush.radialGradient(
//                            colors = listOf(
//                                statusColor.copy(alpha = 0.15f),
//                                Color.Transparent
//                            )
//                        ),
//                        shape = CircleShape
//                    )
//            )
//
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.SpaceBetween
//            ) {
//                // Icon
//                Surface(
//                    modifier = Modifier.size(40.dp),
//                    shape = CircleShape,
//                    color = statusColor.copy(alpha = 0.12f),
//                    border = BorderStroke(2.dp, statusColor.copy(alpha = 0.3f))
//                ) {
//                    Box(contentAlignment = Alignment.Center) {
//                        Icon(
//                            imageVector = statusIcon,
//                            contentDescription = null,
//                            tint = statusColor,
//                            modifier = Modifier.size(22.dp)
//                        )
//                    }
//                }
//
//                // Info
//                Column {
//                    Text(
//                        text = statusText,
//                        fontSize = 24.sp,
//                        fontWeight = FontWeight.ExtraBold,
//                        color = statusColor,
//                        letterSpacing = (-0.5).sp
//                    )
//
//                    Text(
//                        text = "Status Perangkat",
//                        fontSize = 11.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        lineHeight = 14.sp
//                    )
//                }
//            }
//        }
//    }
//}

// Alternatif 3: Inline Status di Header (Minimalis)
@Composable
fun InlineDeviceStatus(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isOnline) Color(0xFF43A047) else Color(0xFFE53935)
    val statusText = if (isOnline) "Online" else "Offline"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = statusColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White // Text putih agar kontras di header
            )
        }
    }
}