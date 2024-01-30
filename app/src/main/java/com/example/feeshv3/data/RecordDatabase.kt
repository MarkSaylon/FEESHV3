package com.example.feeshv3.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Record::class], version = 1, exportSchema = false)
abstract class RecordDatabase: RoomDatabase() {

    abstract fun recordDao(): RecordDAO
    companion object {
        @Volatile
        private var INSTANCE : RecordDatabase? = null

        fun getDatabase(context: Context): RecordDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecordDatabase::class.java,
                    name = "Records"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}