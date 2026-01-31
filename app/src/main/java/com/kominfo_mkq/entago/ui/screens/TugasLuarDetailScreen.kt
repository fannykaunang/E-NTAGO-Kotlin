package com.kominfo_mkq.entago.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kominfo_mkq.entago.data.remote.RetrofitClient.BASE_URL
import com.kominfo_mkq.entago.data.remote.response.TugasLuarData
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TugasLuarDetailScreen(
    navController: NavHostController,
    tugas: TugasLuarData // Data dilempar dari screen sebelumnya
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showFullScreen by remember { mutableStateOf(false) }

    val imageData = if (tugas.isOffline) {
        // Jika offline, ambil langsung dari path file lokal (imagePath)
        tugas.lampiranPath
    } else {
        // Jika online, gabungkan BASE_URL + path dari server
        val baseUrl = BASE_URL.trimEnd('/')
        val cleanPath = tugas.lampiranPath?.trimStart('/') ?: ""
        "$baseUrl/$cleanPath".replace(" ", "")
    }

    val displayDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm", Locale("id", "ID"))
        val date = inputFormat.parse(tugas.tanggal)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        tugas.tanggal // Jika gagal (data offline), tampilkan teks aslinya
    }

    val baseUrl = BASE_URL.trimEnd('/')
    val cleanPath = tugas.lampiranPath?.trimStart('/') ?: ""
    val fullImagePath = "$baseUrl/$cleanPath".replace(" ", "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Tugas", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (tugas.statusVerifikasi == 2 && !tugas.isOffline) {
                        IconButton(onClick = {
                            // Arahkan ke rute edit (misal: "edit_tugas_luar")
                            // Anda bisa mengirimkan ID atau data melalui ViewModel
                            navController.navigate("edit_tugas_luar")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Tugas",
                                tint = MaterialTheme.colorScheme.primary // Warna mencolok untuk aksi
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Header Status & Judul
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadgeLarge(tugas.statusVerifikasi)
                    if (tugas.isOffline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OfflineBadge() // Menggunakan komponen badge yang kita buat sebelumnya
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = tugas.tujuan,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(displayDate, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))

            // 2. Keterangan Tugas
            DetailSection(title = "Keterangan Tugas", icon = Icons.Default.Description) {
                Text(
                    tugas.keterangan,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 22.sp)
            }

            // 3. Lokasi & Peta
            DetailSection(title = "Lokasi & Peta", icon = Icons.Default.LocationOn) {
                Text(
                    text = tugas.alamat,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Tombol Buka Peta
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:${tugas.latitude},${tugas.longitude}?q=${tugas.latitude},${tugas.longitude}(${tugas.tujuan})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lihat di Google Maps")
                }
            }

            // 4. Lampiran Foto
            DetailSection(title = "Bukti Lampiran", icon = Icons.Default.Image) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clickable { if (!tugas.lampiranPath.isNullOrEmpty()) showFullScreen = true }
                ) {
                    if (tugas.lampiranPath.isNullOrEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.BrokenImage, null, tint = Color.Gray)
                                Text("Tidak ada foto lampiran", color = Color.Gray)
                            }
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageData) // Otomatis handle file:/// atau http://
                                .crossfade(true)
                                .build(),
                            contentDescription = "Klik untuk perbesar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = rememberVectorPainter(Icons.Default.BrokenImage),
                            placeholder = rememberVectorPainter(Icons.Default.Image)
                        )
                    }
                }

                // Info Metadata File
                if (!tugas.lampiranPath.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "File: ${tugas.lampiranPath.substringAfterLast("/")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (showFullScreen) {
                FullScreenImageDialog(
                    imageUrl = imageData ?: "",
                    onDismiss = { showFullScreen = false }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    val context = LocalContext.current

    // State untuk zoom dan geser
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Agar benar-benar full screen
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black // Background hitam agar foto menonjol
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Gambar yang bisa di-zoom
                AsyncImage(
                    model = ImageRequest.Builder(context).data(imageUrl).build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        // Logika Zoom dan Pan (Geser)
                        .graphicsLayer(
                            scaleX = maxOf(1f, scale),
                            scaleY = maxOf(1f, scale),
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state),
                    contentScale = ContentScale.Fit
                )

                // Tombol Tutup di Pojok Kanan Atas
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        content()
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))
}

// Badge Status yang lebih besar untuk header
@Composable
fun StatusBadgeLarge(status: Int) {
    val (text, color, icon) = when (status) {
        1 -> Triple("Disetujui", Color(0xFF43A047), Icons.Default.VerifiedUser)
        2 -> Triple("Menunggu Verifikasi", Color(0xFFFB8C00), Icons.Default.CalendarToday)
        else -> Triple("Ditolak", Color(0xFFE53935), Icons.Default.BrokenImage)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}