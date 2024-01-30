package com.example.feeshv3.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<Record>>
    private val repository: UserRepository

    init {
        val recordDAO = RecordDatabase.getDatabase(application).recordDao()
        repository = UserRepository(recordDAO)
        readAllData = repository.readAllData
    }

    fun addRecord(record: Record){
        viewModelScope.launch  (Dispatchers.IO){
            repository.addRecord(record)
        }
    }

    fun deleteRecord(record: Record) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecord(record)
        }
    }

}