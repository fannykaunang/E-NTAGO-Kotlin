package com.kominfo_mkq.entago.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.ui.viewmodel.RekapBulananViewModel
import com.kominfo_mkq.entago.utils.formatPeriode
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekapBulananScreen(navController: NavHostController, viewModel: RekapBulananViewModel) {
    val data = viewModel.selectedMonthData
    val selectedIndex = viewModel.allRekapData.indexOf(viewModel.selectedMonthData).coerceAtLeast(0)

    LaunchedEffect(Unit) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        viewModel.fetchRekap(currentYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekapitulasi Bulanan", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
    ) { innerPadding ->
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding) // SOLUSI UTAMA: Menerapkan padding dari Scaffold
        .padding(horizontal = 16.dp)) {
        // --- DROPDOWN ATAU TAB PILIH BULAN ---
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedIndex, //viewModel.allRekapData.indexOf(data).coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            val bulanNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
            bulanNames.forEachIndexed { index, name ->
                Tab(
                    // --- PERBAIKAN: Logika selected harus dinamis ---
                    selected = index == selectedIndex,
                    onClick = { viewModel.selectMonth(index) },
                    text = {
                        Text(
                            text = name,
                            fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        } else if (data != null) {
            // --- RINGKASAN PERSENTASE ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.Start) {
                    Text("Persentase Kehadiran", style = MaterialTheme.typography.labelLarge)
                    Text("${data.persentase_Kehadiran}%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Periode ${formatPeriode(data.periode_Bulan)} (${data.total_Hari_Kerja} total hari kerja)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- GRID STATISTIK ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Hadir", "${data.hadir}/${data.total_Hari_Kerja} Hari", Icons.Default.CheckCircle, Modifier.weight(1f))
                StatCard("Total Jam", "${data.total_Jam_Kerja} J", Icons.Default.Schedule, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Izin", "${data.izin} Hari", Icons.Default.Info, Modifier.weight(1f))
                StatCard("Alpa", "${data.alpa} Hari", Icons.Default.Cancel, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ini adalah data real yang diambil dari absensi Anda di perangkat mobile/mesin absen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}