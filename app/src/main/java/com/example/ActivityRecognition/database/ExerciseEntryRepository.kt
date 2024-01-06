package com.example.ActivityRecognition.database

import kotlinx.coroutines.flow.Flow
import kotlin.concurrent.thread

class ExerciseEntryRepository (private val exerciseEntryDatabaseDao: ExerciseEntryDatabaseDao){

    val allExerciseEntry : Flow<List<ExerciseEntry>> = exerciseEntryDatabaseDao.getAllExerciseEntry()

    fun insert(exerciseEntry: ExerciseEntry){
        thread{
            exerciseEntryDatabaseDao.insertExerciseEntry(exerciseEntry)
        }.join()
    }

    fun delete(id: Long){
        thread {
            exerciseEntryDatabaseDao.deleteExerciseEntry(id)
        }.join()
    }

    fun deleteAll(){
        thread {
            exerciseEntryDatabaseDao.deleteAll()
        }.join()
    }
}