package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SalmonSample
import kotlinx.coroutines.flow.Flow

@Dao
interface SalmonSampleDao {
    @Query("SELECT * FROM salmon_samples ORDER BY timestamp DESC")
    fun getAllSamples(): Flow<List<SalmonSample>>

    @Query("SELECT * FROM salmon_samples WHERE cageNumber = :cage ORDER BY timestamp DESC")
    fun getSamplesByCage(cage: String): Flow<List<SalmonSample>>

    @Query("SELECT * FROM salmon_samples WHERE samplingDate = :date ORDER BY timestamp DESC")
    fun getSamplesByDate(date: String): Flow<List<SalmonSample>>

    @Query("SELECT DISTINCT cageNumber FROM salmon_samples ORDER BY cageNumber ASC")
    fun getDistinctCages(): Flow<List<String>>

    @Query("SELECT DISTINCT samplingDate FROM salmon_samples ORDER BY samplingDate DESC")
    fun getDistinctDates(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sample: SalmonSample): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(samples: List<SalmonSample>)

    @Update
    suspend fun update(sample: SalmonSample)

    @Delete
    suspend fun delete(sample: SalmonSample)

    @Query("DELETE FROM salmon_samples WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM salmon_samples")
    suspend fun clearAll()

    @Query("DELETE FROM salmon_samples WHERE cageNumber = :cage AND samplingDate = :date")
    suspend fun deleteByCageAndDate(cage: String, date: String)
}
