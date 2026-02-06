package com.kominfo_mkq.entago.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.ui.theme.Orange
import com.kominfo_mkq.entago.ui.theme.StatusApproved
import com.kominfo_mkq.entago.ui.theme.StatusRejected
import com.kominfo_mkq.entago.ui.viewmodel.DashboardUiState
import com.kominfo_mkq.entago.ui.viewmodel.DashboardViewModel
import com.kominfo_mkq.entago.ui.viewmodel.RiwayatViewModel
import com.kominfo_mkq.entago.utils.LocationUtils
import com.kominfo_mkq.entago.utils.getDeviceId
import com.kominfo_mkq.entago.utils.openWhatsApp

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    riwayatViewModel: RiwayatViewModel,
    viewModel: DashboardViewModel,
    prefManager: PrefManager,
    navController: NavHostController,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState

    val activity = LocalActivity.current

    BackHandler {
        activity?.finish()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notifikasi penting untuk info absensi", Toast.LENGTH_SHORT).show()
        }
    }

    var showPermissionRationale by remember { mutableStateOf(false) }

    //android.util.Log.d("DEBUG_NULL", "Checkin: ${viewModel.lastCheckin}")
    //android.util.Log.d("DEBUG_NULL", "Checkout: ${viewModel.lastCheckout}")
    //android.util.Log.d("DEBUG_NULL", "Persentase: ${riwayatViewModel.attendancePercentage}")
    //android.util.Log.d("DEBUG_NULL", "Jam Kerja: ${riwayatViewModel.totalWorkHours}")

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var isCheckingLocation by remember { mutableStateOf(false) }
    val currentHardwareId = remember { getDeviceId(context).trim() }

    LaunchedEffect(viewModel.absenMessage) {
        viewModel.absenMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAbsenMessage()
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    // Logika Jam Absen
    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

    val (buttonText, inoutMode, isTimeValid) = when (hour) {
        in 6..9 -> Triple("PRESENSI DATANG", 1, true)
        in 16..18 -> Triple("PRESENSI PULANG", 2, true)
        else -> Triple("DI LUAR JAM ABSEN", -1, false)
    }

    val isAlreadyAbsen = when (inoutMode) {
        1 -> viewModel.lastCheckin.isNotEmpty() && viewModel.lastCheckin != "--:--"
        2 -> viewModel.lastCheckout.isNotEmpty() && viewModel.lastCheckout != "--:--"
        else -> false
    }

    // GPS Launcher
    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            Toast.makeText(context, "GPS Aktif!", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi Verifikasi dan Absen
    fun verifyAndAbsen() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isCheckingLocation = true

            val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client = LocationServices.getSettingsClient(context)

            client.checkLocationSettings(builder.build())
                .addOnSuccessListener {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    )
                        .addOnSuccessListener { location ->
                            isCheckingLocation = false
                            location?.let {
                                if (LocationUtils.isMockLocation(it)) {
                                    Toast.makeText(
                                        context,
                                        "Fake GPS Terdeteksi!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@addOnSuccessListener
                                }

                                val withinRadius = LocationUtils.isWithinRadius(
                                    it.latitude,
                                    it.longitude,
                                    prefManager.getLatitude(),
                                    prefManager.getLongitude()
                                )

                                if (withinRadius) {
                                    viewModel.performAbsensi(currentHardwareId, inoutMode) {
                                        viewModel.loadProfile(currentHardwareId)
                                        riwayatViewModel.fetchRiwayatData(refresh = true)

                                        // Tambahan: Beri feedback ke user
                                        // Toast.makeText(context, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Anda di luar radius kantor!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        .addOnFailureListener {
                            isCheckingLocation = false
                        }
                }
                .addOnFailureListener { exception ->
                    isCheckingLocation = false
                    if (exception is ResolvableApiException) {
                        try {
                            gpsLauncher.launch(
                                IntentSenderRequest.Builder(exception.resolution.intentSender)
                                    .build()
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        }
    }

    // Permission Management
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotif = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            val hasLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

            // Jika salah satu izin penting belum ada, munculkan dialog penjelasan
            if (!hasNotif || !hasLoc) {
                showPermissionRationale = true
            }
        }

        // B. Load Data Profil & Riwayat
        viewModel.loadProfile(currentHardwareId)
        riwayatViewModel.fetchRiwayatData()

        // C. Cek Status Mesin
        android.util.Log.d("DEVICE_MONITOR", "Halaman Dashboard Terbuka, memanggil fungsi...")
        viewModel.checkDeviceStatus()

        viewModel.fetchUnreadCount()
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is DashboardUiState.DeviceMismatch) {
            navController.navigate("device_migration/${uiState.oldDeviceId}") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
    }

    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onConfirm = {
                showPermissionRationale = false
                // Picu permintaan izin sistem setelah user setuju di dialog kita
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                locationPermissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = { showPermissionRationale = false }
        )
    }

    // Animation for button
    val buttonScale by animateFloatAsState(
        targetValue = if (isCheckingLocation || viewModel.isAbsenLoading) 0.95f else 1f,
        animationSpec = tween(100),
        label = "presence_button_scale"
    )

    val scrollState = rememberScrollState()

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
    var showMoreSheet by remember { mutableStateOf(false) }

    // Biar konten scroll tidak ketutup tombol bawah + navbar gesture
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomButtonSpace = 72.dp + 16.dp + bottomInset // tinggi tombol + padding + inset

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                // Panggil fungsi di ViewModel
                viewModel.refreshAllData(riwayatViewModel, currentHardwareId) {
                    isRefreshing = false
                }
            }
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(bottom = bottomButtonSpace)
        ) {
            CompactHeader(
                navController = navController,
                uiState = uiState,
                isDarkMode = isDarkMode,
                isMachineOnline = viewModel.isMachineOnline,
                unreadCount = viewModel.unreadNotificationCount,
                onThemeToggle = onThemeToggle
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-30).dp)
            ) {
                AttendanceSummaryCards(
                    attendancePercent = riwayatViewModel.attendancePercentage ?: "-",
                    workingHours = riwayatViewModel.totalWorkHours ?: "-"
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuickStatsRow(
                    // Gunakan isNullOrEmpty() agar jika NULL tidak crash
                    checkin = if (viewModel.lastCheckin.isNullOrEmpty()) "--:--" else viewModel.lastCheckin,
                    checkout = if (viewModel.lastCheckout.isNullOrEmpty()) "--:--" else viewModel.lastCheckout
                )

//                Spacer(modifier = Modifier.height(16.dp))
//                DeviceStatusCard(
//                    isOnline = false, // Tambahkan state ini di ViewModel
//                    deviceName = "Mesin Absensi"
//                )

                Spacer(modifier = Modifier.height(24.dp))

                CompactMenuGrid(navController, onMoreClick = { showMoreSheet = true })

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        }

        // ===== FIXED BOTTOM BUTTON (NOT SCROLLING) =====
        PresenceButton(
            buttonText = if (isAlreadyAbsen) "SUDAH PRESENSI" else buttonText, // Ubah teks jika sudah absen
            isTimeValid = isTimeValid && !isAlreadyAbsen, // Jika sudah absen, anggap waktu tidak valid lagi (untuk warna)
            isLoading = isCheckingLocation || viewModel.isAbsenLoading,
            inoutMode = inoutMode,
            buttonScale = buttonScale,
            onClick = {
                if (locationPermissionsState.allPermissionsGranted) verifyAndAbsen()
                else locationPermissionsState.launchMultiplePermissionRequest()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding()
        )
        FloatingActionButton(
            onClick = { navController.navigate("ai_chat") }, // Navigasi ke AI Chat
            containerColor = MaterialTheme.colorScheme.tertiaryContainer, // Warna pembeda
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd) // Posisi Kanan Bawah
                .padding(end = 16.dp, bottom = 100.dp) // Bottom 100dp agar di ATAS tombol Presensi
                .navigationBarsPadding() // Agar aman dari gesture bar HP
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome, // Atau Icons.Default.SmartToy
                contentDescription = "Tanya AI"
            )
        }
    }

    if (showMoreSheet) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
        ) {
            // Isi menu tambahan di sini
            MoreMenuSheetContent(
                navController = navController,
                prefManager = prefManager, // Masukkan variabel prefManager di sini
                onDismiss = { showMoreSheet = false }
            )
        }
    }
}

@Composable
fun MoreMenuSheetContent(
    navController: NavHostController,
    prefManager: PrefManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val deviceId = remember { getDeviceId(context) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Menu Lainnya",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 20.dp, start = 8.dp)
        )

        val otherMenus = listOf(
            MenuItem("Status Mesin", Icons.Default.Dns),
            MenuItem("Pengaturan", Icons.Default.Settings),
            MenuItem("Panduan", Icons.Default.Star),
            MenuItem("Hubungi IT", Icons.Default.SupportAgent),
            MenuItem("Keluar", Icons.AutoMirrored.Filled.Logout)
        )

        val menuRows = otherMenus.chunked(4)

        menuRows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                rowItems.forEach { item ->
                    val isLogout = item.title == "Keluar"
                    val tintColor = if (isLogout) Color.Red else MaterialTheme.colorScheme.primary

                    CompactMenuItem(
                        item = item,
                        iconColor = tintColor,
                        onClick = {
                            onDismiss()
                            when (item.title) {
                                "Status Mesin" -> navController.navigate("status_mesin")
                                "Keluar" -> {
                                    prefManager.logout()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                                "Pengaturan" -> navController.navigate("settings")
                                "Hubungi IT" -> {
                                    openWhatsApp(context, prefManager.getNama() ?: "-", prefManager.getSkpd() ?: "-", deviceId)
                                }
                                "Panduan" -> navController.navigate("panduan")
                            }
                        },
                        // PAKAI WEIGHT(1f): Supaya dibagi rata 25% per kolom
                        modifier = Modifier.weight(1f)
                    )
                }

                // KUNCI: Tambahkan Spacer kosong agar item tetap di kolomnya (tidak melebar)
                // Jika di baris tersebut cuma ada 1 item, kita tambah 3 spacer kosong
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AttendanceSummaryCards(
    attendancePercent: String,
    workingHours: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModernStatsCard(
            title = "Persentase Kehadiran", // Label eksplisit
            value = attendancePercent,
            subtitle = "Bulan ini",
            icon = Icons.Default.CheckCircle,
            gradientColors = listOf(
                Color(0xFF1E88E5),
                Color(0xFF1565C0)
            ),
            modifier = Modifier.weight(1f)
        )

        ModernStatsCard(
            title = "Total Jam Kerja", // Label eksplisit
            value = workingHours,
            subtitle = "Bulan ini",
            icon = Icons.Default.Assessment,
            gradientColors = listOf(
                Color(0xFF43A047),
                Color(0xFF2E7D32)
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ModernStatsCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp), // Sedikit ditambah tingginya agar teks muat
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dekorasi Lingkaran
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 60.dp, y = (-20).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = gradientColors.map { it.copy(alpha = 0.15f) }
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Ikon Atas
                Box(
                    modifier = Modifier
                        .size(40.dp) // Ukuran ikon diperkecil sedikit agar lebih proporsional
                        .background(
                            brush = Brush.linearGradient(colors = gradientColors),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = value,
                            fontSize = 24.sp, // Ukuran font disesuaikan
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = subtitle,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // Teks Deskripsi di bawah angka
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, // Dibuat Bold agar lebih jelas
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Badge perlu ini
@Composable
fun CompactHeader(
    navController: NavHostController,
    uiState: DashboardUiState,
    isDarkMode: Boolean,
    isMachineOnline: Boolean,
    unreadCount: Int,
    onThemeToggle: () -> Unit
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current

    val headerColors = if (isDarkMode) {
        listOf(Color(0xFF311B92), Color(0xFF1A237E))
    } else {
        listOf(Color(0xFF6A1B9A), Color(0xFF4527A0))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(colors = headerColors),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ===== LEFT SIDE: Dashboard + Device Status =====
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dashboard",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Device Status Badge di samping Dashboard
                    InlineDeviceStatus(isOnline = isMachineOnline)
                }

                // ===== RIGHT SIDE: Theme & Notification =====
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onThemeToggle, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Ganti Tema",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    androidx.compose.material3.BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                androidx.compose.material3.Badge(
                                    containerColor = Color.Red,
                                    contentColor = Color.White,
                                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                ) {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        IconButton(
                            onClick = { navController.navigate("notification_screen") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }

                is DashboardUiState.DeviceMismatch -> {
                    Text(
                        text = "Memverifikasi Perangkat...",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                is DashboardUiState.Success -> {
                    val pegawai = uiState.data
                    //android.util.Log.d("DEBUG_NULL", "Nama: ${pegawai.pegawai_nama}")
                    //android.util.Log.d("DEBUG_NULL", "NIP: ${pegawai.pegawai_nip}")
                    Column {
                        Text(
                            text = pegawai.pegawai_nama ?: "-",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // HAPUS InlineDeviceStatus dari sini karena sudah dipindah ke atas
                        // Spacer(modifier = Modifier.height(8.dp))
                        // InlineDeviceStatus(isOnline = isMachineOnline)

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val nipValue = pegawai.pegawai_nip ?: ""
                                if (nipValue.isNotEmpty()) {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(nipValue))
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                        Toast.makeText(
                                            context,
                                            "NIP disalin ke clipboard",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "NIP: ${pegawai.pegawai_nip ?: "-"}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin NIP",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${pegawai.jabatan} pada ${pegawai.skpd}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(0.95f)
                        )
                    }
                }

                is DashboardUiState.Error -> {
                    Text(
                        text = "Gagal memuat profil",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStatsRow(checkin: String, checkout: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // KOLOM DATANG
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF43A047).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login, // Diubah ke Login agar sesuai arti "Datang"
                        contentDescription = null,
                        tint = StatusApproved,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Datang",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = checkin,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (checkin == "--:--" || checkin.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else StatusApproved
                    )
                }
            }

            // PEMBATAL TENGAH
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            // KOLOM PULANG
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            StatusRejected.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout, // Tetap Logout untuk "Pulang"
                        contentDescription = null,
                        tint = StatusRejected,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pulang",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = checkout,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (checkout == "--:--" || checkout.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else StatusRejected
                    )
                }
            }
        }
    }
}

@Composable
fun PresenceButton(
    buttonText: String,
    isTimeValid: Boolean,
    isLoading: Boolean,
    inoutMode: Int,
    buttonScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    val containerColor = when {
        !isTimeValid -> cs.surfaceVariant
        inoutMode == 1 -> cs.primary    // Datang
        inoutMode == 2 -> Orange   // Pulang
        else -> cs.primary
    }

    val contentColor = when {
        !isTimeValid -> cs.onSurfaceVariant
        inoutMode == 1 -> cs.onPrimary
        inoutMode == 2 -> cs.onTertiary
        else -> cs.onPrimary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(buttonScale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTimeValid) 4.dp else 0.dp
        )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            enabled = isTimeValid && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContentColor = cs.onSurfaceVariant
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    color = contentColor,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
            }

            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isTimeValid) Icons.Default.Fingerprint else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = contentColor
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = buttonText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = contentColor
                        )
                        if (isTimeValid) {
                            Text(
                                text = "Ketuk untuk presensi",
                                fontSize = 11.sp,
                                color = contentColor.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactMenuGrid(
    navController: NavHostController,
    onMoreClick: () -> Unit
) {
    Column {
        Text(
            text = "Menu Utama",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val menuItems = listOf(
                    MenuItem("Tugas Luar", Icons.AutoMirrored.Filled.DirectionsWalk),
                    MenuItem("Izin/Cuti", Icons.AutoMirrored.Filled.Assignment),
                    MenuItem("Riwayat", Icons.Default.History),
                    MenuItem("Rekapitulasi", Icons.Default.Assessment),
                    MenuItem("Kantor", Icons.Default.LocationOn),
                    MenuItem("Peta Sebaran Tugas Luar", Icons.Default.Map),
                    MenuItem("Profil", Icons.Default.Person),
                    MenuItem("Lainnya", Icons.Default.MoreHoriz)
                )

                val rows = menuItems.chunked(4)
                rows.forEachIndexed { index, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEach { item ->
                            CompactMenuItem(
                                item = item,
                                onClick = {
                                    when (item.title) {
                                        "Riwayat" -> navController.navigate("riwayat")
                                        "Izin/Cuti" -> navController.navigate("izin")
                                        "Kantor" -> navController.navigate("kantor")
                                        "Profil" -> navController.navigate("profile")
                                        "Rekapitulasi" -> navController.navigate("rekapitulasibulanan")
                                        "Tugas Luar" -> navController.navigate("tugas_luar")
                                        "Peta Sebaran Tugas Luar" -> navController.navigate("sebaran_tugas")
                                        "Lainnya" -> onMoreClick()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (index < rows.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CompactMenuItem(
    item: MenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = iconColor.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, iconColor.copy(alpha = 0.12f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (iconColor == StatusRejected) StatusRejected else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAYA MENGERTI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("NANTI SAJA", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        icon = {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "Izin Akses Diperlukan",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Untuk pengalaman absensi yang optimal di E-NTAGO, kami memerlukan izin Notifikasi agar Anda mendapatkan Informasi Libur/Cuti Bersama, serta izin Lokasi untuk memastikan Anda berada dalam radius kantor saat melakukan presensi.",
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                fontSize = 14.sp
            )
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Efek agak transparan
    )
}

data class MenuItem(
    val title: String,
    val icon: ImageVector
)
