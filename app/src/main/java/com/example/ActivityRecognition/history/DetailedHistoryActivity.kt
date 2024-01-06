package com.example.ActivityRecognition.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ActivityRecognition.ExerciseEntryViewModel
import com.example.ActivityRecognition.ExerciseEntryViewModelFactory
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.database.ExerciseEntry
import com.example.ActivityRecognition.database.ExerciseEntryDatabase
import com.example.ActivityRecognition.database.ExerciseEntryDatabaseDao
import com.example.ActivityRecognition.database.ExerciseEntryRepository
import kotlin.properties.Delegates

// Done: add delete function, half work
// Done: add unit preference
class DetailedHistoryActivity : AppCompatActivity() {

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var exerciseEntryViewModel: ExerciseEntryViewModel
    private var id by Delegates.notNull<Long>()
    private var count : Long = 0;

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_history)

        // set up database, only operate on the viewModel layer
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        exerciseEntryViewModel = ViewModelProvider(this, viewModelFactory).get(
            ExerciseEntryViewModel::class.java)

//        val exerciseEntryList : List<ExerciseEntry>? = exerciseEntryViewModel.allExerciseEntryLiveData.value
        exerciseEntryViewModel.allExerciseEntryLiveData.observe(this){
            val exerciseEntryList: ArrayList<ExerciseEntry> = it as ArrayList<ExerciseEntry>
            id = intent.getLongExtra("id", -1)
            println("Detailid::$id")
            val size = exerciseEntryList.size
            println("Detailsize::$size")
            if(id < size){
                val exerciseEntry: ExerciseEntry = exerciseEntryList[id.toInt()]
                println("Detail::" + exerciseEntry)
                // get entity real id
                count = exerciseEntry.id

                // find views that need to fill in the data
                val inputType = findViewById<EditText>(R.id.input_input_type)
                val activityType  = findViewById<EditText>(R.id.input_activity_type)
                val dateTime = findViewById<EditText>(R.id.input_date_time)
                val duration = findViewById<EditText>(R.id.input_duration)
                val distance = findViewById<EditText>(R.id.input_distance)
                val calories = findViewById<EditText>(R.id.input_calories)
                val heartrate = findViewById<EditText>(R.id.input_heartrate)

                // use SharedPreferences here, change data base on unit
                val sp: SharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE)
                val unitPreferences = sp.getString("UnitPreference", "")
                println("unit::$unitPreferences")

                inputType.setText(intToInputType(exerciseEntry.inputType))
                activityType.setText(intToActivityType(exerciseEntry.activityType))
                dateTime.setText(exerciseEntry.dateTime)
                duration.setText(exerciseEntry.duration.toString()+ " secs")
                // change base on unit preference
                distance.setText(exerciseEntry.distance.toString()+" Miles")
                if(unitPreferences=="Metric (Kilometers)"){
                    val dis = (exerciseEntry.distance*1.6).toInt().toDouble()
                    distance.setText("$dis Kilometers")
                }
//            if(unitPreferences=="Imperial (Miles)"){
//                distance.setText(exerciseEntry.distance.toString()+"Miles")
//            }
                calories.setText(exerciseEntry.calorie.toString() + " cals")
                heartrate.setText(exerciseEntry.heartRate.toString()+ " bpm")
            }
        }
    }

    // Todo: currently only implement the "manual entry part"
    fun intToInputType(i : Int) : String{
        if(i == 1){
            return "Manual Entry"
        }
        else{
            return "???"
        }
    }

    fun intToActivityType(i : Int) : String{
        if(i == 1){
            return "Running"
        }
        else if(i == 2){
            return "Walking"
        }
        else if(i == 3){
            return "Standing"
        }
        else if(i == 4){
            return "Cycling"
        }
        else if(i == 5){
            return "Hiking"
        }
        else if(i == 6){
            return "Downhill Skiing"
        }
        else if(i == 7){
            return "Cross-Country Skiing"
        }
        else if(i == 8){
            return "Snowboarding"
        }
        else if(i == 9){
            return "Skating"
        }
        else if(i == 10){
            return "Swimming"
        }
        else if(i == 11){
            return "Mountain Biking"
        }
        else if(i == 12){
            return "Wheelchair"
        }
        else if(i == 13){
            return "Elliptical"
        }
        else{
            return "Others"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, 1, 1, "back")
        menu.add(Menu.NONE, 2, 2, "delete")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == 1){
            Toast.makeText(applicationContext, "back", Toast.LENGTH_SHORT).show()
            finish()
        }
        if(item.itemId == 2){
            Toast.makeText(applicationContext, "delete", Toast.LENGTH_SHORT).show()
            exerciseEntryViewModel.delete(count)
            finish()
        }
        return true
    }


}