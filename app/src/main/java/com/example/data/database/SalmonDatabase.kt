package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CageTargetDao
import com.example.data.dao.SalmonSampleDao
import com.example.data.model.CageTarget
import com.example.data.model.SalmonSample

@Database(entities = [SalmonSample::class, CageTarget::class], version = 2, exportSchema = false)
abstract class SalmonDatabase : RoomDatabase() {
    abstract fun salmonSampleDao(): SalmonSampleDao
    abstract fun cageTargetDao(): CageTargetDao

    companion object {
        @Volatile
        private var INSTANCE: SalmonDatabase? = null

        fun getDatabase(context: Context): SalmonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalmonDatabase::class.java,
                    "salmon_sampling_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
