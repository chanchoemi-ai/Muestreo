package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cage_targets")
data class CageTarget(
    @PrimaryKey
    val cageNumber: String,
    val targetWeightGrams: Double,
    val updatedAt: Long = System.currentTimeMillis()
)
