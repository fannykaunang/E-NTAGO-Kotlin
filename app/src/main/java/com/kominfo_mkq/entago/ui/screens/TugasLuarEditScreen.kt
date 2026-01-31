package com.kominfo_mkq.entago.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.kominfo_mkq.entago.data.remote.RetrofitClient.BASE_URL
import com.kominfo_mkq.entago.data.remote.response.TugasLuarData
import com.kominfo_mkq.entago.ui.viewmodel.TugasLuarEditViewModel
import com.kominfo_mkq.entago.ui.viewmodel.TugasLuarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TugasLuarEditScreen(
    navController: NavHostController,
    viewModel: TugasLuarEditViewModel,
    sharedViewModel: TugasLuarViewModel,
    tugasAsal: TugasLuarData
) {
    val context = LocalContext.current

    // Inisialisasi data hanya sekali saat pertama kali masuk
    LaunchedEffect(Unit) {
        viewModel.initData(tugasAsal, BASE_URL.trimEnd('/'))
    }

    // Gunakan struktur UI yang sama dengan AddTugasLuarScreen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Tugas Luar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {

            // Preview Foto (Tampilkan foto lama atau foto baru jika sudah dipotret)
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp).clickable { /* Logika Ambil Foto */ },
                shape = RoundedCornerShape(16.dp)
            ) {
                AsyncImage(
                    model = viewModel.imageUri ?: viewModel.oldImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormInput(value = viewModel.tujuan, onValueChange = { viewModel.tujuan = it }, label = "Tujuan", icon = Icons.Default.Flag)
            Spacer(modifier = Modifier.height(12.dp))
            FormInput(value = viewModel.alamat, onValueChange = { viewModel.alamat = it }, label = "Alamat", icon = Icons.Default.Map)
            Spacer(modifier = Modifier.height(12.dp))
            FormInput(value = viewModel.keterangan, onValueChange = { viewModel.keterangan = it }, label = "Keterangan", icon = Icons.Default.Description, singleLine = false, modifier = Modifier.height(100.dp))

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.updateTugas(context) {
                        val dataBaru = tugasAsal.copy(
                            tujuan = viewModel.tujuan,
                            alamat = viewModel.alamat,
                            keterangan = viewModel.keterangan,
                            // lampiranPath diupdate jika ada upload foto baru (opsional sesuai logika backend)
                        )

                        // 2. Update Source of Truth di Shared ViewModel
                        sharedViewModel.selectedTugas = dataBaru

                        // 3. Baru kembali ke halaman detail
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Simpan Perubahan")
            }
        }
    }
}