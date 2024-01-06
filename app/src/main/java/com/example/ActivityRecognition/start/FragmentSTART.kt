package com.example.ActivityRecognition.start

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ActivityRecognition.ExerciseEntryViewModel
import com.example.ActivityRecognition.ExerciseEntryViewModelFactory
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.Util
import com.example.ActivityRecognition.database.ExerciseEntry
import com.example.ActivityRecognition.database.ExerciseEntryDatabase
import com.example.ActivityRecognition.database.ExerciseEntryDatabaseDao
import com.example.ActivityRecognition.database.ExerciseEntryRepository
import com.example.ActivityRecognition.map.MapActivity
import kotlin.properties.Delegates

class FragmentSTART : Fragment(){

    private var dataSize by Delegates.notNull<Int>()

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var exerciseEntryViewModel: ExerciseEntryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // create view first, need to use in findViewById()
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        // find the two spinner in the layout
        val inputTypeSpinner : Spinner = view.findViewById(R.id.inputTypeSpinner)
        val activityTypeSpinner : Spinner = view.findViewById(R.id.activityTypeSpinner)

        // set up database, only operate on the viewModel layer
        database = ExerciseEntryDatabase.getInstance(requireActivity())
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        exerciseEntryViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseEntryViewModel::class.java)
        // use it to get exerciseEntryList and then get the size
        exerciseEntryViewModel.allExerciseEntryLiveData.observe(viewLifecycleOwner) {
            val exerciseEntryList: ArrayList<ExerciseEntry> = it as ArrayList<ExerciseEntry>
            dataSize = exerciseEntryList.size
            // when start button is clicked, go to corresponding activity through intent and use intent to pass the value of chosen option
            val startButton: Button = view.findViewById(R.id.startButton)
            startButton.setOnClickListener() {
                if (inputTypeSpinner.selectedItem.toString() == "Manual Entry") {
                    // pass the value through intent to ManualEntryActivity
                    val intent = Intent(context, ManualEntryActivity::class.java)
                    intent.putExtra("dataSize", dataSize)
                    intent.putExtra("inputType", "Manual Entry")
                    // here activityType can only be create inside the if
                    val activityType: String = activityTypeSpinner.selectedItem.toString()
                    intent.putExtra("activityType", activityType)
                    startActivity(intent)
                } else if (inputTypeSpinner.selectedItem.toString() == "GPS") {

                    Util.checkLocPermission(requireActivity())

                    val intent = Intent(context, MapActivity::class.java)
                    intent.putExtra("dataSize", dataSize)
                    intent.putExtra("inputType", "GPS Entry")
                    val activityType: String = activityTypeSpinner.selectedItem.toString()
                    intent.putExtra("activityType", activityType)
                    startActivity(intent)

//                    val serviceIntent = Intent(context, TrackingService::class.java)
//                    requireActivity().startService(serviceIntent)
                } else {
                    // Todo: Automatic here, need implement
                    Util.checkLocPermission(requireActivity())

                    val intent = Intent(context, MapActivity::class.java)
                    intent.putExtra("dataSize", dataSize)
                    intent.putExtra("inputType", "Automatic Entry")
                    val activityType: String = activityTypeSpinner.selectedItem.toString()
                    intent.putExtra("activityType", activityType)
                    startActivity(intent)
                }
            }
        }
        return view
    }
}