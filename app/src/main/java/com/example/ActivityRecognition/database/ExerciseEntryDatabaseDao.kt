package com.example.ActivityRecognition.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseEntryDatabaseDao {

    @Insert
    fun insertExerciseEntry(exerciseEntry: ExerciseEntry)

    @Query("SELECT * FROM ExerciseEntry_table")
    fun getAllExerciseEntry(): Flow<List<ExerciseEntry>>

    @Query("DELETE FROM ExerciseEntry_table")
    fun deleteAll()

    @Query("DELETE FROM ExerciseEntry_table WHERE id = :key")
    //":" indicates Bind variable
    fun deleteExerciseEntry(key: Long)




}