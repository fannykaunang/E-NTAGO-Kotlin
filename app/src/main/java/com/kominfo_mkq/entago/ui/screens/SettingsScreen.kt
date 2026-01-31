package com.kominfo_mkq.entago.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.data.local.AppDatabase
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.ui.viewmodel.SettingsViewModel
import com.kominfo_mkq.entago.utils.getDeviceId
import com.kominfo_mkq.entago.utils.openWhatsApp
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    prefManager: PrefManager,
    onThemeToggle: () -> Unit,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    var isBiometricActive by remember { mutableStateOf(prefManager.isBiometricEnabled()) }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    var showClearCacheDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.message) {
        if (viewModel.message.isNotEmpty()) {
            Toast.makeText(context, viewModel.message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pengaturan", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header Card (Versi Aplikasi)
            SettingsHeaderCard()

            val deviceId = remember { getDeviceId(context) }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Seksi Akun & Keamanan
            SettingsSection(title = "Akun & Keamanan", icon = Icons.Default.Security) {
                ClickableSettingsItem(
                    label = "Ubah Kata sandi",
                    subLabel = "Amankan akses akun Anda",
                    icon = Icons.Default.Lock,
                    onClick = { showPasswordDialog = true }
                )
                SwitchSettingsItem(
                    label = "Autentikasi Biometrik",
                    subLabel = "Masuk dengan sidik jari",
                    icon = Icons.Default.Fingerprint,
                    checked = isBiometricActive,
                    onCheckedChange = { isChecked ->
                        isBiometricActive = isChecked
                        prefManager.setBiometricEnabled(isChecked)
                    }
                )
            }

            // 3. Preferensi Aplikasi
            SettingsSection(title = "Preferensi", icon = Icons.Default.Tune) {
                SwitchSettingsItem(
                    label = "Mode Gelap (Dark Mode)",
                    subLabel = "Sesuaikan tampilan layar",
                    icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    checked = isDarkMode,
                    onCheckedChange = { onThemeToggle() }
                )
                SwitchSettingsItem(
                    label = "Notifikasi Pengingat",
                    subLabel = "Ingatkan jadwal absen",
                    icon = Icons.Default.NotificationsActive,
                    checked = true,
                    onCheckedChange = { /* Toggle Notif */ }
                )
            }

            // 4. Data & Sinkronisasi
            SettingsSection(title = "Data & Penyimpanan", icon = Icons.Default.SdStorage) {
                ClickableSettingsItem(
                    label = "Sinkronisasi Manual",
                    subLabel = "Kirim laporan offline tertunda",
                    icon = Icons.Default.Sync,
                    onClick = {
                        // Ambil DAO dari database
                        val db = AppDatabase.getDatabase(context)
                        val dao = db.tugasLuarDao()

                        // Panggil fungsi sinkronisasi
                        viewModel.syncManualTugasLuar(dao)
                    }
                )
                ClickableSettingsItem(
                    label = "Bersihkan Cache",
                    subLabel = "Hapus file sementara foto",
                    icon = Icons.Default.DeleteSweep,
                    onClick = { showClearCacheDialog = true }
                )
            }

            // 5. Informasi & Bantuan
            SettingsSection(title = "Bantuan", icon = Icons.AutoMirrored.Filled.HelpCenter) {
                ClickableSettingsItem(label = "Panduan Pengguna", icon = Icons.AutoMirrored.Filled.MenuBook, onClick = { })
                ClickableSettingsItem(label = "Hubungi IT Kominfo", icon = Icons.Default.SupportAgent, onClick = {
                    openWhatsApp(
                        context = context,
                        nama = prefManager.getNama() ?: "-",
                        skpd = prefManager.getSkpd() ?: "-",
                        deviceId = deviceId
                    )
                })
                ClickableSettingsItem(
                    label = "Tentang E-NTAGO",
                    icon = Icons.Default.Info,
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                            "https://entago.merauke.go.id/tentang".toUri())
                        context.startActivity(intent)
                    }
                )
                ClickableSettingsItem(
                    label = "Kebijakan Privasi",
                    icon = Icons.Default.PrivacyTip,
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                            "https://entago.merauke.go.id/kebijakan-privasi".toUri())
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Ubah Kata Sandi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Kata Sandi Lama") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Kata Sandi Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (viewModel.message.isNotEmpty()) {
                        Text(
                            text = viewModel.message,
                            color = MaterialTheme.colorScheme.error, // Mendukung Dark Mode
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePassword(oldPassword, newPassword) {
                            showPasswordDialog = false
                            oldPassword = ""
                            newPassword = ""
                        }
                    },
                    enabled = !viewModel.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Menyesuaikan Tema
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (viewModel.isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                    else Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    viewModel.clearMessage()
                }) {
                    Text("Batal", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Bersihkan Cache?") },
            text = { Text("Ini akan menghapus file foto sementara yang sudah tidak terpakai untuk menghemat ruang penyimpanan HP Anda. Foto draft yang belum terkirim tidak akan dihapus.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearCacheDialog = false
                        val db = AppDatabase.getDatabase(context)
                        viewModel.clearPhotoCache(context, db.tugasLuarDao())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Bersihkan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SettingsHeaderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "E-NTAGO MERAUKE",
                    color = MaterialTheme.colorScheme.onPrimary, // Pastikan teks kontras dengan gradient
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
                Text(
                    text = "Versi Aplikasi 2.0.4-stable",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column { content() }
        }
    }
}

@Composable
fun ClickableSettingsItem(label: String, subLabel: String? = null, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (subLabel != null) Text(subLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SwitchSettingsItem(label: String, subLabel: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
//bikin fungsi sinkron manual sama bersihkan cache
@Composable
fun SettingsIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
    }
}