package com.kominfo_mkq.entago.ui.screens

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.ui.login.findActivity
import com.kominfo_mkq.entago.ui.theme.GradientEndDark
import com.kominfo_mkq.entago.ui.theme.GradientStartDark
import com.kominfo_mkq.entago.ui.viewmodel.ActivationUiState
import com.kominfo_mkq.entago.ui.viewmodel.DeviceActivationViewModel
import com.kominfo_mkq.entago.utils.getDeviceId

@Composable
fun DeviceActivationScreen(
    navController: NavHostController,
    viewModel: DeviceActivationViewModel
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val deviceId = remember { getDeviceId(context) }

    LaunchedEffect(viewModel.uiState) {
        if (viewModel.uiState is ActivationUiState.Success) {
            navController.navigate("dashboard") {
                popUpTo("device_activation") { inclusive = true }
            }
        }
    }

    // Setup Biometric
    val biometricPrompt = remember(activity) {
        activity?.let {
            BiometricPrompt(it, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    // Panggil fungsi langsung dari viewModel yang sudah kita pasang sebagai parameter
                    viewModel.registerDevice(deviceId)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Opsional: handle error biometrik di sini
                }
            })
        }
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Daftarkan Perangkat")
        .setSubtitle("Gunakan Sidik Jari atau PIN HP untuk mengunci akun di perangkat ini")
        .setAllowedAuthenticators(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    val gradient = Brush.verticalGradient(
        colors = listOf(GradientStartDark, GradientEndDark)
    )

    Box(modifier = Modifier.fillMaxSize().background(gradient)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Bagian luar Card tetap Putih karena backgroundnya Gradient Ungu (Brand Identity)
            Icon(
                imageVector = Icons.Default.PhonelinkLock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Daftarkan Perangkat Anda",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Akun E-NTAGO Anda belum terikat pada perangkat manapun. Untuk mulai melakukan absensi, silakan daftarkan HP ini sebagai perangkat resmi Anda.",
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // === CARD DIMULAI ===
            // Di sini kita ubah agar background Card mengikuti tema (Putih/Hitam)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface // Dinamis
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Device ID Anda:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Abu-abu dinamis
                    )
                    Text(
                        text = deviceId,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, // Hitam di Light, Putih di Dark
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (viewModel.uiState is ActivationUiState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary // Warna Primary Tema
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = {
                                    // Pemicu sidik jari sebelum panggil API
                                    biometricPrompt?.authenticate(promptInfo)
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary, // Warna Primary Tema
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("AKTIVASI SEKARANG", fontWeight = FontWeight.Bold)
                            }

                            // Tampilkan Pesan Error jika Gagal
                            if (viewModel.uiState is ActivationUiState.Error) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = (viewModel.uiState as ActivationUiState.Error).message,
                                    color = MaterialTheme.colorScheme.error, // Merah sesuai tema
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}