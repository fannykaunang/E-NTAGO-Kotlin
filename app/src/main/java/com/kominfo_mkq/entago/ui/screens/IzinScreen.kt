package com.kominfo_mkq.entago.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.data.remote.response.IzinItem
import com.kominfo_mkq.entago.ui.components.EmptyState
import com.kominfo_mkq.entago.ui.viewmodel.IzinUiState
import com.kominfo_mkq.entago.ui.viewmodel.IzinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IzinScreen(navController: NavHostController, viewModel: IzinViewModel, isDarkMode: Boolean) {
    val uiState = viewModel.uiState

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Cek apakah ada sinyal refresh dari halaman sebelumnya
                val isRefreshNeeded = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<Boolean>("refresh_flag") == true

                if (isRefreshNeeded) {
                    // Lakukan refresh data
                    viewModel.fetchIzinList()

                    // Reset sinyal agar tidak refresh terus menerus
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_flag", false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // State UI Lokal
    var isSearching by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        // --- INPUT PENCARIAN DI TOOLBAR ---
                        TextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.searchQuery = it },
                            placeholder = { Text("Cari alasan...", style = MaterialTheme.typography.bodyLarge) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    } else {
                        Text(
                            text = "Daftar Izin/Cuti",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    // --- TOMBOL TOGGLE SEARCH ---
                    IconButton(onClick = {
                        isSearching = !isSearching
                        if (!isSearching && viewModel.searchQuery.isNotEmpty()) {
                            // Opsional: Kosongkan search jika ditutup, atau biarkan filter aktif
                            viewModel.searchQuery = ""
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Cari"
                        )
                    }

                    // --- TOMBOL FILTER CATEGORY ---
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (viewModel.selectedCategory != "Semua") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            viewModel.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            category,
                                            color = if (viewModel.selectedCategory == category) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (viewModel.selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectedCategory = category
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("izin_add") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Izin")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Penting agar tidak tertutup TopBar
                .background(MaterialTheme.colorScheme.background)
        ) {

            // --- CHIP FILTER AKTIF ---
            ActiveIzinFiltersRow(viewModel, isDarkMode)

            // --- LIST DATA ---
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is IzinUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is IzinUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is IzinUiState.Success -> {
                        val dataTampil = viewModel.filteredData
                        if (dataTampil.isEmpty()) {
                            EmptyState(
                                isSearching = isSearching,
                                title = "Belum Izin/Cuti",
                                description = "Tekan tombol + di pojok bawah untuk membuat Izin/Cuti baru"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(dataTampil) { izin ->
                                    IzinCard(
                                        item = izin,
                                        onClick = {
                                            // Kirim izin_Urutan (bukan izin_Id)
                                            navController.navigate("izin_detail/${izin.izin_Urutan}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveIzinFiltersRow(
    viewModel: IzinViewModel,
    isDarkMode: Boolean
) {
    val isFilterActive = viewModel.searchQuery.isNotEmpty() || viewModel.selectedCategory != "Semua"
    val primaryColor = MaterialTheme.colorScheme.primary

    if (isFilterActive) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menggunakan List Pair untuk loop sederhana
            val filterItems = mutableListOf<Pair<String, () -> Unit>>()

            if (viewModel.searchQuery.isNotEmpty()) {
                filterItems.add("\"${viewModel.searchQuery}\"" to { viewModel.searchQuery = "" })
            }
            if (viewModel.selectedCategory != "Semua") {
                filterItems.add(viewModel.selectedCategory to { viewModel.selectedCategory = "Semua" })
            }

            filterItems.forEach { (label, onRemove) ->
                InputChip(
                    selected = true,
                    onClick = onRemove,
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF1A1C1E)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isDarkMode) Color.White else Color(0xFF1A1C1E)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = if (isDarkMode) primaryColor.copy(alpha = 0.2f) else Color(0xFFE2E8F0),
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        enabled = true,
                        selected = true,
                        selectedBorderColor = if (isDarkMode) primaryColor.copy(alpha = 0.5f) else Color(0xFFCBD5E1),
                        selectedBorderWidth = 1.dp
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = {
                viewModel.searchQuery = ""
                viewModel.selectedCategory = "Semua"
            }) {
                Text("Reset", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun IzinCard(item: IzinItem, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    val tanggal = try {
        if (item.izin_Tgl != null && item.izin_Tgl.length >= 10) {
            val date = dateFormat.parse(item.izin_Tgl)
            displayFormat.format(date!!)
        } else {
            item.izin_Tgl ?: "-"
        }
    } catch (_: Exception) {
        item.izin_Tgl ?: "-"
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Indikator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Nama Kategori Izin
                Text(
                    text = item.kat_Izin_Nama,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ===== TANGGAL & JAM DENGAN ICON =====
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Calendar icon + Tanggal
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tanggal,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Separator & Clock icon + Jam (jika ada)
                    if (!item.izin_Noscan_Time.isNullOrEmpty()) {
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )

                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.izin_Noscan_Time,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status Badge
                Spacer(modifier = Modifier.height(6.dp))
//                val (statusText, badgeColor) = when (item.izin_Status) {
//                    1 -> "Disetujui" to Color(0xFF43A047)
//                    2 -> "Ditolak" to Color(0xFFE53935)
//                    else -> "Proses" to Color(0xFFFB8C00)
//                }
                val (statusText, badgeColor) = when (item.izin_Status) {
                    0 -> "Ditolak" to Color(0xFFE53935)       // Merah (Ditolak)
                    1 -> "Disetujui" to Color(0xFF43A047)     // Hijau (Diterima)
                    2 -> "Menunggu Review" to Color(0xFFFB8C00) // Orange (Baru/Review)
                    else -> "Proses" to Color.Gray            // Fallback (Jika ada status lain)
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chevron Right Icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Lihat detail",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
