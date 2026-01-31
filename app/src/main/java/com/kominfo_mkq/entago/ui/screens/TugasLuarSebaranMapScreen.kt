package com.kominfo_mkq.entago.ui.screens

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.data.remote.response.TugasLuarData
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import com.kominfo_mkq.entago.R
import com.kominfo_mkq.entago.ui.viewmodel.TugasLuarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TugasLuarSebaranMapScreen(
    navController: NavHostController,
    viewModel: TugasLuarViewModel
) {
    val context = LocalContext.current
    val tugasList = viewModel.allTugas // Data gabungan Online + Offline

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sebaran Lokasi Tugas", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osm_pref", 0))
                MapView(ctx).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(13.0)
                    controller.setCenter(GeoPoint(-8.489, 140.401))
                }
            },
            update = { mapView ->
                // 1. Bersihkan overlay lama agar tidak menumpuk saat data refresh
                mapView.overlays.clear()

                val clusterer = MyMarkerClusterer(context)
                mapView.overlays.add(clusterer)

                // 3. Tambahkan Marker dari data terbaru
                tugasList.forEach { tugas ->
                    val lat = tugas.latitude.toDoubleOrNull()
                    val lng = tugas.longitude.toDoubleOrNull()

                    if (lat != null && lng != null) {
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(lat, lng)

                        // --- LOGIKA HEADER & DETAIL ---
                        marker.title = tugas.tujuan      // Muncul di bubble_title
                        marker.snippet = tugas.keterangan // Muncul di bubble_description

                        // Menggunakan Custom InfoWindow
                        val infoWindow = CustomInfoWindow(mapView, tugas)
                        marker.infoWindow = infoWindow

                        marker.icon = if (tugas.isOffline) {
                            ContextCompat.getDrawable(context, R.drawable.ic_marker_offline)
                        } else {
                            ContextCompat.getDrawable(context, R.drawable.ic_marker_online)
                        }

                        // Agar saat marker diklik, bubble muncul di posisi yang benar
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        clusterer.add(marker)
                    }
                }

                // 4. KRUSIAL: Beritahu peta untuk menggambar ulang (Refresh)
                mapView.invalidate()
            },
            modifier = Modifier.padding(padding).fillMaxSize()
        )
    }
}

class CustomInfoWindow(mapView: MapView, private val tugas: TugasLuarData) :
    MarkerInfoWindow(R.layout.layout_info_window, mapView) {

    override fun onOpen(item: Any?) {
        val title = mView.findViewById<TextView>(R.id.bubble_title)
        val description = mView.findViewById<TextView>(R.id.bubble_description)

        // Set data ke TextView
        title.text = tugas.tujuan
        description.text = tugas.keterangan

        // Tambahkan event klik pada bubble jika ingin buka detail lengkap
        mView.setOnClickListener {
            close() // Tutup bubble saat diklik
        }
    }
}

class MyMarkerClusterer(context: Context) : RadiusMarkerClusterer(context) {
    init {
        // Karena ini subclass, kita punya akses ke mTextPaint
        mTextPaint.color = Color.WHITE
        mTextPaint.textSize = 40f
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.isFakeBoldText = true

        // Anda juga bisa mengganti icon lingkaran cluster jika mau:
        // setIcon(ContextCompat.getDrawable(context, R.drawable.ic_cluster_circle)?.toBitmap())
    }
}