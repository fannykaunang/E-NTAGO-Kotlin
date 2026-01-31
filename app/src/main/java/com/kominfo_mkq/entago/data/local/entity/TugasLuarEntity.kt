package com.kominfo_mkq.entago.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "offline_tugas_luar",
    indices = [Index(value = ["tujuan", "keterangan_tugas", "alamat"], unique = true)]
)
data class TugasLuarEntity(
    @PrimaryKey(autoGenerate = true) val idLocal: Int = 0,
    val tujuan: String,
    val keterangan_tugas: String,
    val alamat: String,
    val latitude: String,
    val longitude: String,
    val imagePath: String
)