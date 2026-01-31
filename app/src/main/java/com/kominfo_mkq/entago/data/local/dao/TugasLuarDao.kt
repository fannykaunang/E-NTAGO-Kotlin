package com.kominfo_mkq.entago.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kominfo_mkq.entago.data.local.entity.TugasLuarEntity

@Dao
interface TugasLuarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(tugas: TugasLuarEntity)

    @Query("SELECT * FROM offline_tugas_luar")
    suspend fun getAllDrafts(): List<TugasLuarEntity>

    @Delete
    suspend fun deleteDraft(tugas: TugasLuarEntity)

    @Query("SELECT * FROM offline_tugas_luar WHERE tujuan = :tujuan AND keterangan_tugas = :keterangan AND alamat = :alamat LIMIT 1")
    suspend fun checkDuplicate(tujuan: String, keterangan: String, alamat: String): TugasLuarEntity?
}