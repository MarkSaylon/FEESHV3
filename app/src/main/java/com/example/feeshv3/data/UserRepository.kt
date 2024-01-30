package com.example.feeshv3.data

import androidx.lifecycle.LiveData

class UserRepository(private val recordDao: RecordDAO) {
    val readAllData: LiveData<List<Record>> = recordDao.readAllData()

    suspend fun addRecord(record: Record){
        recordDao.addRecord(record)
    }

    suspend fun deleteRecord(record: Record) {
        recordDao.deleteRecord(record)

    }
}