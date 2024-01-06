package com.example.ActivityRecognition

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.ActivityRecognition.database.ExerciseEntry
import com.example.ActivityRecognition.database.ExerciseEntryRepository
import java.lang.IllegalArgumentException

class ExerciseEntryViewModel(private val repository: ExerciseEntryRepository) : ViewModel() {
    val allExerciseEntryLiveData: LiveData<List<ExerciseEntry>> = repository.allExerciseEntry.asLiveData()

    fun insert(exerciseEntry: ExerciseEntry) {
        repository.insert(exerciseEntry)
    }

    fun delete(id: Long){
        repository.delete(id)
    }

    fun deleteAll(){
        val exerciseEntryList = allExerciseEntryLiveData.value
        if (exerciseEntryList != null && exerciseEntryList.isNotEmpty())
            repository.deleteAll()
    }

    fun getSize() : Int{
        val exerciseEntryList = allExerciseEntryLiveData.value
        if (exerciseEntryList != null) {
            return exerciseEntryList.size
        }
        return 10
    }
}

class ExerciseEntryViewModelFactory (private val repository: ExerciseEntryRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass, which is CommentViewModel in this case.
        if(modelClass.isAssignableFrom(ExerciseEntryViewModel::class.java))
            return ExerciseEntryViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}