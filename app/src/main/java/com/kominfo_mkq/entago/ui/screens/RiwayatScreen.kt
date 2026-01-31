package com.kominfo_mkq.entago.ui.screens

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.data.remote.response.RiwayatItem
import com.kominfo_mkq.entago.ui.viewmodel.RiwayatUiState
import com.kominfo_mkq.entago.ui.viewmodel.RiwayatViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(
    navController: NavHostController,
    viewModel: RiwayatViewModel,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val uiState = viewModel.riwayatState
    val refreshState = rememberPullToRefreshState()

    // State UI
    var isSearching by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    // Dialog Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.startDate = dateRangePickerState.selectedStartDateMillis
                    viewModel.endDate = dateRangePickerState.selectedEndDateMillis
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("Pilih Rentang Tanggal", modifier = Modifier.padding(16.dp)) },
                modifier = Modifier.height(450.dp)
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.searchQuery = it },
                            placeholder = { Text("Ketik hari atau tanggal...", style = MaterialTheme.typography.bodyLarge) },
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
                        Text("Riwayat Absensi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // 1. Tombol Search Toggle
                    IconButton(onClick = {
                        isSearching = !isSearching
                        if (!isSearching && viewModel.searchQuery.isNotEmpty()) {
                            // Opsional: viewModel.searchQuery = ""
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Cari"
                        )
                    }

                    // 2. Menu Dropdown (Filter & PDF)
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opsi",
                                tint = if (viewModel.startDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Filter Tanggal") },
                                leadingIcon = { Icon(Icons.Default.DateRange, null) },
                                onClick = { showMenu = false; showDatePicker = true }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Cetak PDF") },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) },
                                onClick = {
                                    showMenu = false
                                    generatePdf(context, viewModel.filteredData, viewModel.pegawaiName, viewModel.pegawaiNip)
                                }
                            )
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            // Filter Chip Row (Seragam dengan IzinScreen)
            ActiveRiwayatFiltersRow(viewModel, isDarkMode)

            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = { viewModel.fetchRiwayatData(refresh = true) },
                state = refreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                when (uiState) {
                    is RiwayatUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is RiwayatUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = uiState.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        }
                    }
                    is RiwayatUiState.Success -> {
                        val dataTampil = viewModel.filteredData

                        if (dataTampil.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text("Data tidak ditemukan", color = Color.Gray)
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            ) {
                                val listState = rememberLazyListState()

                                // Deteksi Scroll Bottom untuk Load More
                                val reachedBottom = remember {
                                    derivedStateOf {
                                        val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                                        lastVisibleItem?.index != 0 && lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1
                                    }
                                }

                                LaunchedEffect(reachedBottom.value) {
                                    if (reachedBottom.value) viewModel.loadMoreData()
                                }

                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Header Tabel
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        TableHeader("Hari", Modifier.weight(1f))
                                        TableHeader("Tanggal", Modifier.weight(1.5f))
                                        TableHeader("Masuk", Modifier.weight(1f))
                                        TableHeader("Pulang", Modifier.weight(1f))
                                    }

                                    LazyColumn(state = listState) {
                                        items(dataTampil) { item ->
                                            RiwayatRow(item)
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                        }

                                        // Loading indicator di bawah list jika sedang load more
                                        if (viewModel.isLoadingMore) {
                                            item {
                                                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiwayatRow(item: RiwayatItem) {
    val activeSource = if (!item.scan_In.isNullOrEmpty()) item.scan_In else item.scan_Out

    val parts = activeSource?.split(", ") ?: emptyList()
    val hari = item.hari ?: "-"

    val dateFull = parts.getOrNull(1) ?: ""
    val dateParts = dateFull.split(" ")
    val tanggal = if (dateParts.size >= 2) "${dateParts[0]} ${dateParts[1]}" else "-"

    val jamIn = item.scan_In?.split(" ")?.lastOrNull() ?: "--:--"
    val jamOut = item.scan_Out?.split(" ")?.lastOrNull() ?: "--:--"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = hari,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = tanggal,
            modifier = Modifier.weight(1.5f), // Disesuaikan dengan header
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = jamIn,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = if (jamIn == "--:--") MaterialTheme.colorScheme.outline else Color(0xFF43A047),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = jamOut,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = if (jamOut == "--:--") MaterialTheme.colorScheme.outline else Color(0xFFE53935),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TableHeader(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = Color.White, // Header selalu putih karena background primary
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRiwayatFiltersRow(
    viewModel: RiwayatViewModel,
    isDarkMode: Boolean
) {
    val isFilterActive = viewModel.searchQuery.isNotEmpty() || viewModel.startDate != null
    val chipDateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val primaryColor = MaterialTheme.colorScheme.primary

    if (isFilterActive) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filterItems = mutableListOf<Pair<String, () -> Unit>>()

            if (viewModel.searchQuery.isNotEmpty()) {
                filterItems.add("\"${viewModel.searchQuery}\"" to { viewModel.searchQuery = "" })
            }
            if (viewModel.startDate != null && viewModel.endDate != null) {
                val dateText = "${chipDateFormat.format(Date(viewModel.startDate!!))} - ${chipDateFormat.format(Date(viewModel.endDate!!))}"
                filterItems.add(dateText to { viewModel.startDate = null; viewModel.endDate = null })
            }

            filterItems.forEach { (label, onRemove) ->
                InputChip(
                    selected = true,
                    onClick = onRemove,
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            // Kontras: Putih di Dark Mode, Hitam Slate di Light Mode
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
                    colors = InputChipDefaults.inputChipColors(
                        // Background: Transparan Primary di Dark, Abu Slate di Light
                        selectedContainerColor = if (isDarkMode) primaryColor.copy(alpha = 0.2f) else Color(0xFFE2E8F0),
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        enabled = true,
                        selected = true,
                        selectedBorderColor = if (isDarkMode) primaryColor.copy(alpha = 0.5f) else Color(0xFFCBD5E1),
                        selectedBorderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { viewModel.clearFilters() }) {
                Text("Reset", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Fungsi Generate PDF (Tidak berubah logicnya, hanya formatting)
fun generatePdf(
    context: android.content.Context,
    dataList: List<RiwayatItem>,
    namaPegawai: String,
    nipPegawai: String
) {
    val pdfDocument = PdfDocument()
    val paint = Paint()
    val headerPaint = Paint()

    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas

    // 1. Judul Laporan
    headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    headerPaint.textSize = 16f
    canvas.drawText("LAPORAN ABSENSI PEGAWAI", 40f, 40f, headerPaint)
    canvas.drawText("E-NTAGO KABUPATEN MERAUKE", 40f, 65f, headerPaint)

    // 2. Info Pegawai
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Nama    : $namaPegawai", 40f, 100f, paint)
    canvas.drawText("NIP        : $nipPegawai", 40f, 120f, paint)

    // Garis Header
    canvas.drawLine(40f, 135f, 550f, 135f, paint)

    // 3. Header Tabel
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    var yPosition = 160f
    canvas.drawText("Hari", 40f, yPosition, paint)
    canvas.drawText("Tanggal", 120f, yPosition, paint)
    canvas.drawText("Masuk", 280f, yPosition, paint)
    canvas.drawText("Pulang", 400f, yPosition, paint)

    canvas.drawLine(40f, yPosition + 10f, 550f, yPosition + 10f, paint)

    // 4. Isi Data
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    yPosition += 40f

    dataList.forEach { item ->
        if (yPosition > 800f) { // Simple pagination check
            // Note: Proper pagination requires creating new page loops, omitted for brevity
        }

        val scanIn = item.scan_In ?: ""
        val parts = scanIn.split(", ")
        val hari = parts.getOrNull(0) ?: "-"
        val tanggal = parts.getOrNull(1)?.split(" ")?.take(3)?.joinToString(" ") ?: "-"
        val jamIn = if (scanIn.isNotEmpty()) scanIn.split(" ").last() else "--:--"
        val jamOut = if (item.scan_Out?.isNotEmpty() == true) item.scan_Out!!.split(" ").last() else "--:--"

        canvas.drawText(hari, 40f, yPosition, paint)
        canvas.drawText(tanggal, 120f, yPosition, paint)
        canvas.drawText(jamIn, 280f, yPosition, paint)
        canvas.drawText(jamOut, 400f, yPosition, paint)

        yPosition += 25f
    }

    pdfDocument.finishPage(page)

    val fileName = "Riwayat_${System.currentTimeMillis()}.pdf"
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF Tersimpan di Download", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal simpan PDF", Toast.LENGTH_SHORT).show()
    }
    pdfDocument.close()
}