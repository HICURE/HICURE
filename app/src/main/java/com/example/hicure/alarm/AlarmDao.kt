package com.example.hicure.alarm

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Query("SELECT * FROM table_alarm")
    fun getAllAlarm() : Flow<List<AlarmEntity>>

    @Insert
    fun insertAlarm(alarmEntity: AlarmEntity)

    @Update
    fun updateAlarm(alarmEntity: AlarmEntity)

    @Delete
    fun deleteAlarm(alarmEntity: AlarmEntity)
}