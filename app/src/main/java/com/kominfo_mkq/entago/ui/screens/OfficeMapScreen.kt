package com.kominfo_mkq.entago.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.data.local.PrefManager
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeMapScreen(
    navController: NavHostController,
    prefManager: PrefManager,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val officeLat = prefManager.getLatitude()
    val officeLng = prefManager.getLongitude()
    val officePoint = GeoPoint(officeLat, officeLng)

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_pref", 0))
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lokasi Kantor & Radius", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setMultiTouchControls(true)
                        controller.setZoom(18.5)
                        controller.setCenter(officePoint)

                        // Efek Dark Mode (Invert Colors)
                        if (isDarkMode) {
                            overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                        }

                        // 1. TAMBAHKAN RADIUS (Polygon Lingkaran)
                        val circle = Polygon(this)
                        circle.points = Polygon.pointsAsCircle(officePoint, 1000.0) // 100 Meter
                        circle.fillPaint.color = android.graphics.Color.parseColor("#336A1B9A") // Ungu transparan
                        circle.outlinePaint.color = android.graphics.Color.parseColor("#6A1B9A")
                        circle.outlinePaint.strokeWidth = 2f
                        overlays.add(circle)

                        // 2. MARKER KANTOR
                        val marker = Marker(this)
                        marker.position = officePoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "Kantor/Dinas Anda"
                        // Gunakan ikon pin point jika ada, atau default marker
                        overlays.add(marker)

                        // 3. LOKASI REALTIME PEGAWAI
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        locationOverlay.enableMyLocation() // Menampilkan titik biru
                        overlays.add(locationOverlay)
                    }
                }
            )
        }
    }
}