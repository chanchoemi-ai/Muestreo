package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CageTarget
import kotlinx.coroutines.flow.Flow

@Dao
interface CageTargetDao {
    @Query("SELECT * FROM cage_targets")
    fun getAllTargets(): Flow<List<CageTarget>>

    @Query("SELECT * FROM cage_targets WHERE cageNumber = :cageNumber LIMIT 1")
    fun getTargetForCage(cageNumber: String): Flow<CageTarget?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(target: CageTarget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(targets: List<CageTarget>)

    @Query("DELETE FROM cage_targets WHERE cageNumber = :cageNumber")
    suspend fun delete(cageNumber: String)
}
