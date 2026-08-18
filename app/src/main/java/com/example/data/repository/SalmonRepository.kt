package com.example.data.repository

import com.example.data.dao.CageTargetDao
import com.example.data.dao.SalmonSampleDao
import com.example.data.model.CageTarget
import com.example.data.model.SalmonSample
import kotlinx.coroutines.flow.Flow

class SalmonRepository(
    private val dao: SalmonSampleDao,
    private val cageTargetDao: CageTargetDao
) {
    val allSamples: Flow<List<SalmonSample>> = dao.getAllSamples()
    val distinctCages: Flow<List<String>> = dao.getDistinctCages()
    val distinctDates: Flow<List<String>> = dao.getDistinctDates()
    val allCageTargets: Flow<List<CageTarget>> = cageTargetDao.getAllTargets()

    fun getSamplesByCage(cage: String): Flow<List<SalmonSample>> = dao.getSamplesByCage(cage)
    fun getSamplesByDate(date: String): Flow<List<SalmonSample>> = dao.getSamplesByDate(date)
    fun getTargetForCage(cage: String): Flow<CageTarget?> = cageTargetDao.getTargetForCage(cage)

    suspend fun insertSample(sample: SalmonSample): Long = dao.insert(sample)
    suspend fun insertAll(samples: List<SalmonSample>) = dao.insertAll(samples)
    suspend fun updateSample(sample: SalmonSample) = dao.update(sample)
    suspend fun deleteSample(sample: SalmonSample) = dao.delete(sample)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun clearAll() = dao.clearAll()
    suspend fun deleteByCageAndDate(cage: String, date: String) = dao.deleteByCageAndDate(cage, date)

    suspend fun setCageTarget(cageNumber: String, targetGrams: Double) {
        cageTargetDao.insertOrUpdate(CageTarget(cageNumber = cageNumber, targetWeightGrams = targetGrams))
    }

    suspend fun setAllCageTargets(targets: List<CageTarget>) {
        cageTargetDao.insertOrUpdateAll(targets)
    }
}

