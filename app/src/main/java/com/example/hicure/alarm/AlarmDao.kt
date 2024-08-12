package com.example.hicure.alarm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM table_alarm")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity)

    @Update
    suspend fun update(alarm: AlarmEntity)

    @Query("SELECT * FROM table_alarm WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?


    @Query("DELETE FROM table_alarm WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM table_alarm")
    suspend fun deleteAllAlarms()

}
