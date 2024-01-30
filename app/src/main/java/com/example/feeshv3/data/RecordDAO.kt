package com.example.feeshv3.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecordDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRecord (record: Record)

    @Query("SELECT * FROM Records ORDER BY id ASC")
    fun readAllData(): LiveData<List<Record>>

    @Delete
    suspend fun deleteRecord(record: Record)
}