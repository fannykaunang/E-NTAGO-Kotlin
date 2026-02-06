package com.kominfo_mkq.entago.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.ui.viewmodel.DeviceMigrationViewModel
import com.kominfo_mkq.entago.ui.viewmodel.MigrationUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceMigrationScreen(
    oldDeviceId: String,
    navController: NavHostController,
    viewModel: DeviceMigrationViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentDeviceId = remember { com.kominfo_mkq.entago.utils.getDeviceId(context) }
    var otpValue by remember { mutableStateOf("") }

    val maskedId = remember(oldDeviceId) {
        val truncated = oldDeviceId.take(20)
        if (truncated.length > 12) {
            truncated.dropLast(12) + "************"
        } else {
            "************"
        }
    }

    LaunchedEffect(viewModel.uiState) {
        if (viewModel.uiState is MigrationUiState.Success) {
            navController.navigate("dashboard") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // BACKGROUND UTAMA: Tetap Gradient Ungu agar Brand Identity terjaga
            // (Tidak perlu ikut Dark Mode karena ini 'Full Screen Hero')
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF6A1B9A), Color(0xFF311B92))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon & Judul Atas (Tetap Putih karena backgroundnya Ungu Gelap)
            Icon(
                Icons.Default.PhonelinkSetup,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Pindah Perangkat",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Akun Anda terikat di perangkat lain:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // BOX ID Perangkat
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(oldDeviceId))
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                Toast.makeText(context, "ID Perangkat disalin", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = maskedId,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin ID",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // === CARD PUTIH DI TENGAH ===
            // Di sini kita mulai mainkan Tema Gelap/Terang
            Card(
                shape = RoundedCornerShape(24.dp),
                // PENTING: Gunakan 'surface' agar otomatis Hitam/Putih sesuai tema
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // JIKA BELUM KIRIM OTP
                    if (viewModel.uiState is MigrationUiState.Idle || (viewModel.uiState is MigrationUiState.Error && viewModel.maskedPhone.isEmpty())) {
                        Text(
                            "Verifikasi WhatsApp",
                            fontWeight = FontWeight.Bold,
                            // Warna teks mengikuti tema (Hitam di Light, Putih di Dark)
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Kami akan mengirimkan kode verifikasi ke nomor WhatsApp Anda yang terdaftar.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            // Gunakan onSurfaceVariant untuk teks sekunder (abu-abu dinamis)
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.requestOtp(currentDeviceId) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = viewModel.uiState !is MigrationUiState.SendingOtp
                        ) {
                            if (viewModel.uiState is MigrationUiState.SendingOtp) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            } else {
                                Text("KIRIM KODE OTP")
                            }
                        }
                    }
                    // JIKA OTP SUDAH TERKIRIM
                    else {
                        Text(
                            "Masukkan Kode OTP",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface, // Dinamis
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Kode telah dikirim ke WhatsApp Anda:\n${viewModel.maskedPhone}",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary, // Warna primer aplikasi
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = otpValue,
                            onValueChange = { if (it.length <= 6) otpValue = it },
                            label = { Text("Kode OTP") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            // Hapus hardcode warna, biarkan Material 3 mengaturnya
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.verifyOtp(otpValue, currentDeviceId) },
                            enabled = otpValue.length >= 4 && viewModel.uiState !is MigrationUiState.Verifying,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (viewModel.uiState is MigrationUiState.Verifying) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            } else {
                                Text("VERIFIKASI & PINDAH HP")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { viewModel.requestOtp(currentDeviceId) },
                            enabled = viewModel.resendTimer == 0 && viewModel.uiState !is MigrationUiState.SendingOtp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (viewModel.resendTimer > 0) {
                                Text(
                                    text = "Kirim ulang tersedia dalam ${viewModel.resendTimer}s",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Abu dinamis
                                    fontSize = 13.sp
                                )
                            } else {
                                Text(
                                    text = "Belum terima kode? Kirim ulang",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // TAMPILAN ERROR
                    if (viewModel.uiState is MigrationUiState.Error) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer, // Merah muda dinamis
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                (viewModel.uiState as MigrationUiState.Error).message,
                                color = MaterialTheme.colorScheme.onErrorContainer, // Merah tua dinamis
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}