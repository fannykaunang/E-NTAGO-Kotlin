package com.kominfo_mkq.entago.ui.screens

import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kominfo_mkq.entago.ui.viewmodel.IzinAddViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- PENTING: Import Eksplisit ini memperbaiki error 'overrides nothing' ---
import androidx.compose.material3.SelectableDates
// --------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IzinAddScreen(
    navController: NavHostController,
    viewModel: IzinAddViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // File Picker Launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectedFileUri = it
        }
    }

    // --- LOGIKA BATAS TANGGAL (HARI INI s/d 7 HARI KEDEPAN) ---
    val dateConstraints = remember {
        // PENTING: Gunakan TimeZone UTC karena DatePicker bekerja dalam UTC
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        // Reset jam ke 00:00:00 (Start of Day)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val minDate = calendar.timeInMillis // Hari ini

        calendar.add(Calendar.DAY_OF_YEAR, 7) // Tambah 7 Hari
        val maxDate = calendar.timeInMillis // Batas Akhir

        Pair(minDate, maxDate)
    }

    // Date Picker State dengan Validasi
    var showDateRangePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            // PERBAIKAN: Gunakan 'isSelectableDate' (ada kata 'Date' di belakangnya)
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis in dateConstraints.first..dateConstraints.second
            }

            // Opsional: Pastikan tahun juga bisa dipilih
            override fun isSelectableYear(year: Int): Boolean {
                return true
            }
        }
    )

    if (showDateRangePicker) {
        val configuration = LocalConfiguration.current
        val newConfiguration = Configuration(configuration)
        newConfiguration.setLocale(Locale("id", "ID"))

        CompositionLocalProvider(LocalConfiguration provides newConfiguration) {
            DatePickerDialog(
                onDismissRequest = { showDateRangePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.tanggalMulai = datePickerState.selectedStartDateMillis
                        viewModel.tanggalSelesai = datePickerState.selectedEndDateMillis
                        showDateRangePicker = false
                    }) { Text("PILIH") }
                },
                dismissButton = {
                    TextButton(onClick = { showDateRangePicker = false }) { Text("BATAL") }
                }
            ) {
                DateRangePicker(
                    state = datePickerState,
                    modifier = Modifier.weight(1f),
                    title = {
                        Text(
                            text = "Pilih Rentang Tanggal",
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                        )
                    },
                    headline = {
                        val startMillis = datePickerState.selectedStartDateMillis
                        val endMillis = datePickerState.selectedEndDateMillis
                        val localeID = Locale("id", "ID")
                        val formatter = SimpleDateFormat("dd/MM/yyyy", localeID)

                        val headerText = if (startMillis != null) {
                            val startText = formatter.format(Date(startMillis))
                            val endText = if (endMillis != null) " - ${formatter.format(Date(endMillis))}" else ""
                            "$startText$endText"
                        } else {
                            "Mulai - Selesai"
                        }

                        Text(
                            text = headerText,
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    showModeToggle = true // Matikan input manual agar user pakai kalender
                )
            }
        }
    }

    val (defaultHour, defaultMinute) = remember(viewModel.inputJam) {
        if (viewModel.inputJam.contains(":")) {
            try {
                val parts = viewModel.inputJam.split(":")
                parts[0].toInt() to parts[1].toInt()
            } catch (_: Exception) {
                7 to 30 // Fallback jika error parsing
            }
        } else {
            7 to 30 // Default jika kosong
        }
    }
    // Time Picker State
    val timePickerDialog = remember(defaultHour, defaultMinute) {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                viewModel.inputJam = String.format("%02d:%02d", hourOfDay, minute)
            },
            defaultHour,
            defaultMinute,
            true // Is 24 Hour View
        )
    }

    // Handle Success Navigation
    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            Toast.makeText(context, viewModel.submitMessage, Toast.LENGTH_LONG).show()
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_flag", true)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Pengajuan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: JENIS & KATEGORI ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Detail Izin", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    // 1. Dropdown Jenis Izin
                    AppDropdown(
                        label = "Jenis Izin/Cuti",
                        items = viewModel.listJenisIzin,
                        selectedItem = viewModel.selectedJenis,
                        itemLabel = { it.izin_Jenis_Name },
                        onItemSelected = { item ->
                            viewModel.selectedJenis = item
                            viewModel.selectedKategori = null // Reset kategori

                            // --- LOGIKA SET JAM OTOMATIS ---
                            when (item.izin_Jenis_Id) {
                                101 -> viewModel.inputJam = "16:10" // Tidak Scan Pulang
                                100, 73 -> viewModel.inputJam = "07:30" // Tidak Scan Masuk / Apel
                                else -> viewModel.inputJam = "" // Reset untuk yang lain
                            }
                        }
                    )

                    // 2. Dropdown Kategori
                    if (viewModel.selectedJenis != null) {
                        AppDropdown(
                            label = "Alasan/Kategori",
                            items = viewModel.selectedJenis!!.kategori,
                            selectedItem = viewModel.selectedKategori,
                            itemLabel = { it.kat_Izin_Nama },
                            onItemSelected = { viewModel.selectedKategori = it }
                        )
                    }

                    // 3. Dropdown Cuti Normatif
                    if (viewModel.showCutiNormatifDropdown) {
                        AppDropdown(
                            label = "Pilih Jenis Cuti Normatif",
                            items = viewModel.listCutiNormatif,
                            selectedItem = viewModel.selectedCutiNormatif,
                            itemLabel = { it.cuti_N_Nama },
                            onItemSelected = { viewModel.selectedCutiNormatif = it }
                        )
                        Text(
                            text = "*Pilihan ini akan otomatis ditambahkan ke catatan.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // --- SECTION 2: WAKTU ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Waktu Pelaksanaan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    // Input Tanggal (Range)
                    val dateText = if (viewModel.tanggalMulai != null && viewModel.tanggalSelesai != null) {
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                        "${sdf.format(Date(viewModel.tanggalMulai!!))} - ${sdf.format(Date(viewModel.tanggalSelesai!!))}"
                    } else ""

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = dateText,
                            onValueChange = {},
                            label = { Text("Pilih Tanggal (Dari - Sampai)") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Icon(Icons.Default.DateRange, null) },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Box(
                            Modifier
                                .matchParentSize()
                                .clickable { showDateRangePicker = true }
                        )
                    }

                    // Input Jam
                    if (viewModel.showTimeInput) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = viewModel.inputJam,
                                onValueChange = {},
                                label = { Text("Jam Kejadian") },
                                placeholder = { Text("08:00") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { timePickerDialog.show() }) {
                                        Icon(Icons.Default.AccessTime, null)
                                    }
                                }
                            )
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .clickable { timePickerDialog.show() }
                            )
                        }
                    }
                }
            }

            // --- SECTION 3: DOKUMEN & CATATAN ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    if (viewModel.showFileUploader) {
                        Text("Dokumen Pendukung", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        if (viewModel.selectedFileUri == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { fileLauncher.launch(arrayOf("image/*", "application/pdf")) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.UploadFile, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Upload Bukti (Img/PDF)", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                    Text("Maks 5MB", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, null, tint = Color(0xFF2E7D32))
                                Spacer(Modifier.width(8.dp))
                                Text("File dipilih", modifier = Modifier.weight(1f), fontSize = 14.sp)
                                IconButton(onClick = { viewModel.selectedFileUri = null }) {
                                    Icon(Icons.Default.Close, null, tint = Color.Red)
                                }
                            }
                        }
                    }

                    Text("Keterangan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = viewModel.catatan,
                        onValueChange = { viewModel.catatan = it },
                        label = { Text("Tulis alasan atau keterangan...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TOMBOL SUBMIT ---
            Button(
                onClick = { viewModel.submitForm(context) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isSubmitting
            ) {
                if (viewModel.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(viewModel.submitMessage ?: "Mengirim...")
                } else {
                    Text("Kirim Pengajuan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Komponen Helper untuk Dropdown tetap sama seperti sebelumnya...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = if (selectedItem != null) itemLabel(selectedItem) else "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}