package com.kominfo_mkq.entago.data.remote.request

data class CheckoutRequest(
    val pin: Int,
    val sn: String,
    val inoutmode: Int, // Wajib: 2
    val att_id: String  // Wajib: ddmmmmyyyyhhmmss + sn
)