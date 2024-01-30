package com.example.feeshv3.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity (tableName = "Records")
class Record(
    @PrimaryKey(autoGenerate = true) val id: Int, val temperature: Double,
    val salinity: Double, val pH: Double, val dissolved: Double, val time: String
) {
}