package com.example.ActivityRecognition.start

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.example.ActivityRecognition.ExerciseEntryViewModel
import com.example.ActivityRecognition.ExerciseEntryViewModelFactory
import com.example.ActivityRecognition.MyDialog
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.database.ExerciseEntry
import com.example.ActivityRecognition.database.ExerciseEntryDatabase
import com.example.ActivityRecognition.database.ExerciseEntryDatabaseDao
import com.example.ActivityRecognition.database.ExerciseEntryRepository
import java.util.*
import kotlin.properties.Delegates

class ManualEntryActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{

    private val DATA = arrayOf(
        "Date",
        "Time",
        "Duration",
        "Distance",
        "Calories",
        "Heart Rate",
        "Comment"
    )

    private lateinit var myListView: ListView

    private val calendar = Calendar.getInstance()

    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var exerciseEntryViewModel: ExerciseEntryViewModel

    private var dataSize by Delegates.notNull<Int>()

    private lateinit var date : String
    private lateinit var time : String
    private lateinit var dateSet : String
    private lateinit var timeSet : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)
        title = "MyRuns3-TheDatabase"

        // set up listview for manual entry and connect adapter
        myListView = findViewById(R.id.myListView)
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1, DATA
        )
        myListView.adapter = arrayAdapter

        // this is the bundle used for exchange data between MyDialog and ManualEntryActivity
        val bundle = Bundle()

        // open corresponding dialog fragment according to id/position
        myListView.setOnItemClickListener() { parent, view, position, id ->
            println("debugMyListView:: parent: $parent | view: $view | position: $position | id: $id")
            if (id.compareTo(0) == 0) {
                onDateClicked()
            } else if (id.compareTo(1) == 0) {
                onTimeClicked()
            } else if (id.compareTo(2) == 0) {
                val myDialog = MyDialog()
                bundle.putInt(MyDialog.DIALOG_KEY, id.toInt())
                myDialog.arguments = bundle
                myDialog.show(supportFragmentManager, "duration dialog")
            } else if (id.compareTo(3) == 0) {
                val myDialog = MyDialog()
                bundle.putInt(MyDialog.DIALOG_KEY, id.toInt())
                myDialog.arguments = bundle
                myDialog.show(supportFragmentManager, "distance dialog")
            } else if (id.compareTo(4) == 0) {
                val myDialog = MyDialog()
                bundle.putInt(MyDialog.DIALOG_KEY, id.toInt())
                myDialog.arguments = bundle
                myDialog.show(supportFragmentManager, "calories dialog")
            } else if (id.compareTo(5) == 0) {
                val myDialog = MyDialog()
                bundle.putInt(MyDialog.DIALOG_KEY, id.toInt())
                myDialog.arguments = bundle
                myDialog.show(supportFragmentManager, "heart rate dialog")
            } else if (id.compareTo(6) == 0) {
                val myDialog = MyDialog()
                bundle.putInt(MyDialog.DIALOG_KEY, id.toInt())
                myDialog.arguments = bundle
                myDialog.show(supportFragmentManager, "comment dialog")
            }
        }
        // find save and cancel button
        saveButton = findViewById(R.id.myListViewSaveButton)
        cancelButton = findViewById(R.id.myListViewCancelButton)
        // set up database, only operate on the viewModel layer
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        exerciseEntryViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseEntryViewModel::class.java)

        // get the time when user open ManualEntryActivity
        // in case the user did not click the time or date item
        // then just save the current time
        val tempCalendar : Calendar = Calendar.getInstance()

        val year = tempCalendar.get(Calendar.YEAR)
        val month = tempCalendar.get(Calendar.MONTH) + 1
        val day = tempCalendar.get(Calendar.DAY_OF_MONTH)
        date = "$month-$day-$year"

        val hour = tempCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = tempCalendar.get(Calendar.MINUTE)
        val second = tempCalendar.get(Calendar.SECOND)
        time = "$hour:$minute:$second"

        saveButton.setOnClickListener() {
            // get inputId, inputType, activityType passed by Intent from FragmentSTART
            dataSize = intent.getIntExtra("dataSize", -1)
            val inputType : String? = intent.getStringExtra("inputType")
            val activityType : String? = intent.getStringExtra("activityType")
            // get data pass by bundle from MyDialog
            val duration = bundle.getString("input_duration")
            val distance = bundle.getString("input_distance")
            val calories = bundle.getString("input_calories")
            val heartrate = bundle.getString("input_heartrate")
            val comment = bundle.getString("input_comment")
            // check data
            println("Tag0::" + dataSize)
            println("Tag1::" + inputType)
            println("Tag2::" + activityType)
            println("Tag3::" + date)
            println("Tag4::" + time)
            println("Tag5::" + duration)
            println("Tag6::" + distance)
            println("Tag7::" + calories)
            println("Tag8::" + heartrate)
            println("Tag9::" + comment)

            // Todo: for myruns3, currently only save id, inputType, activityType, dateTime, duration, distance, calorie, heartRate,comment
            // save data to Entity
            val exerciseEntry = ExerciseEntry()
            // here id : primary key is automated generated
//            exerciseEntry.id = inputId.toLong()
            exerciseEntry.inputType = inputTypeToInt(inputType)
            exerciseEntry.activityType = activityTypeToInt(activityType)
            exerciseEntry.dateTime = "$time $date"

            // if no input, default value to 0 and " "
            // else save input to entity
            if (duration == null) {
                exerciseEntry.duration = (0).toDouble()
            }
            else{
                exerciseEntry.duration = duration.toInt().toDouble()
            }
            if (distance == null) {
                exerciseEntry.distance = (0).toDouble()
            }
            else{
                exerciseEntry.distance = distance.toInt().toDouble()
            }
            if (calories == null) {
                exerciseEntry.calorie = (0).toDouble()
            }
            else{
                exerciseEntry.calorie = calories.toInt().toDouble()
            }
            if (heartrate == null) {
                exerciseEntry.heartRate = (0).toDouble()
            }
            else{
                exerciseEntry.heartRate = heartrate.toInt().toDouble()
            }
            if (comment == null) {
                exerciseEntry.comment = " "
            }
            else{
                exerciseEntry.comment = comment
            }

            // insert entity to database through viewmodel
            exerciseEntryViewModel.insert(exerciseEntry)

            Toast.makeText(this, "Entry #$dataSize saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        cancelButton.setOnClickListener() {
            // if not save then id++ need use this id-- to eliminate
//            inputId -= 1
            Toast.makeText(this, "Entry discarded", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun onTimeClicked() {
        //#3
        val timePickerDialog = TimePickerDialog(
            this, this,
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        )
        timePickerDialog.show()
    }

    fun onDateClicked() {
        //#5
        val datePickerDialog = DatePickerDialog(
            this, this,calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // if user click time item, save the input time
    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        timeSet = "$p1:$p2:00"
        time = timeSet
    }
    // if user click Date item, save the input Date
    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        // month + 1 as month get is always -1 smaller
        val month = p2+1
        dateSet = "$month-$p3-$p1"
        date = dateSet
    }

    // as inputType in database is saved as Int, convert from String to Int
    // Todo: currently only implement the "manual entry part" as need, add GPS and Automatic later
    fun inputTypeToInt(inputType: String?) : Int{
        return if(inputType == "Manual Entry"){
            1
        } else{
            0
        }
    }
    // as activityType in database is saved as Int, convert from String to Int
    fun activityTypeToInt(activityType: String?) : Int{
        if(activityType == "Running"){
            return 1
        }
        else if(activityType == "Walking"){
            return 2
        }
        else if(activityType == "Standing"){
            return 3
        }
        else if(activityType == "Cycling"){
            return 4
        }
        else if(activityType == "Hiking"){
            return 5
        }
        else if(activityType == "Downhill Skiing"){
            return 6
        }
        else if(activityType == "Cross-Country Skiing"){
            return 7
        }
        else if(activityType == "Snowboarding"){
            return 8
        }
        else if(activityType == "Skating"){
            return 9
        }
        else if(activityType == "Swimming"){
            return 10
        }
        else if(activityType == "Mountain Biking"){
            return 11
        }
        else if(activityType == "Wheelchair"){
            return 12
        }
        else if(activityType == "Elliptical"){
            return 13
        }
        else{
            return 14
        }
    }
}