package com.kominfo_mkq.entago.data.remote.request

data class CheckinRequest(
    val pegawai_id: Int,
    val pin: Int,
    val sn: String,
    val verifymode: Int = 1,
    val inoutmode: Int, // 0: Datang, 1: Pulang
    val reserved: Int = 0,
    val work_code: Int = 0,
    val att_id: String,
    val scan_date: String
)