package com.example.hicure.alarm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_alarm")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val time: String,
    val amPm: String, // 필요없음
    val label: String,
    var isEnabled: Boolean,
    val isSoundAndVibration: Boolean,
)