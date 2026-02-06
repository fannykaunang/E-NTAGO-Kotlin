package com.kominfo_mkq.entago.data.remote.response

data class RequestMigrationResponse(
    val success: Boolean,
    val message: String,
    val data: MigrationOtpData?
)

data class MigrationOtpData(
    val pin: String,
    val phone_masked: String,
    val expires_at: String
)

data class VerifyMigrationResponse(
    val success: Boolean,
    val message: String,
    val data: VerifyMigrationData?
)

data class VerifyMigrationData(
    val pin: String,
    val phone_masked: String,
    val verified_at: String
)