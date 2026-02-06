package com.kominfo_mkq.entago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kominfo_mkq.entago.data.model.ChatMessage
import com.kominfo_mkq.entago.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll ke bawah saat ada pesan baru
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Icon menyesuaikan warna primary
                        Icon(Icons.Default.SmartToy, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            // Teks otomatis menyesuaikan onSurface (Hitam di Light, Putih di Dark)
                            Text(
                                "Asisten AI E-NTAGO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Online",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant // Abu-abu dinamis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    // Background AppBar mengikuti tema
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                // PENTING: Gunakan background dari tema, jangan hardcode abu-abu
                .background(MaterialTheme.colorScheme.background)
                .imePadding() // Fix Keyboard tertutup
        ) {
            // --- LIST PESAN ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pesan Pembuka
                item {
                    ChatBubble(
                        message = ChatMessage(text = "Namek, Namuk. Entago! Saya Asisten AI E-NTAGO Anda. Silakan tanya soal izin cuti, lupa scan, atau aturan jam kerja.", isFromUser = false),
                        isUser = false
                    )
                }

                items(viewModel.messages) { msg ->
                    ChatBubble(message = msg, isUser = msg.isFromUser)
                }
            }

            // --- INPUT FIELD ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Background area ketik mengikuti Surface (Putih di Light, Abu Gelap di Dark)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ketik pesan...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    // Pengaturan warna TextField agar terlihat bagus di Dark & Light
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 3
                )

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    // Tombol kirim menggunakan warna Primary
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Kirim")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isUser: Boolean) {
    // --- LOGIKA WARNA DINAMIS ---

    // Bubble User: Primary Color (Misal Ungu/Biru)
    // Bubble Bot: SurfaceVariant (Abu muda di Light, Abu tua di Dark)
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    // Warna Teks: Menyesuaikan background bubble
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary // Putih
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant // Hitam (Light) / Putih (Dark)
    }

    val align = if (isUser) Alignment.End else Alignment.Start

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
        // Nama Pengirim (Opsional, kecil di atas bubble)
        if (!isUser) {
            Text(
                text = "Asisten",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp, // Sedikit lancip di ujung chat
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            if (message.isTyping) {
                Text(
                    "Sedang mengetik...",
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                )
            } else {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}