package com.example.ActivityRecognition.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ActivityRecognition.ExerciseEntryViewModel
import com.example.ActivityRecognition.ExerciseEntryViewModelFactory
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.database.ExerciseEntry
import com.example.ActivityRecognition.database.ExerciseEntryDatabase
import com.example.ActivityRecognition.database.ExerciseEntryDatabaseDao
import com.example.ActivityRecognition.database.ExerciseEntryRepository

class FragmentHISTORY : Fragment() {

    private lateinit var historyListView: ListView

    private lateinit var exerciseEntryList: ArrayList<ExerciseEntry>
    private lateinit var exerciseEntryAdapter: FragmentHISTORYListAdapter

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
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // set up listview for history fragment and connect adapter
        historyListView = view.findViewById(R.id.myHistoryListView)
        exerciseEntryList = ArrayList()
        exerciseEntryAdapter = FragmentHISTORYListAdapter(requireActivity(), exerciseEntryList)
        historyListView.adapter = exerciseEntryAdapter

        // set up database, only operate on the viewModel layer
        database = ExerciseEntryDatabase.getInstance(requireActivity())
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        exerciseEntryViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            ExerciseEntryViewModel::class.java)
        // update data instantly
        exerciseEntryViewModel.allExerciseEntryLiveData.observe(requireActivity(), Observer { it ->
            exerciseEntryAdapter.replace(it)
            exerciseEntryAdapter.notifyDataSetChanged()
        })

        historyListView.setOnItemClickListener() { parent, view, position, id ->
            println("HisItemDebug1: parent: $parent | view: $view | position: $position | id: $id")

            val text : TextView = view.findViewById(R.id.bolded)

            println("text: "+ text.text)

            if(text.text[0] == 'G'){
                val intent = Intent(context, MapHistoryActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
            else if(text.text[0] == 'A'){
                val intent = Intent(context, MapHistoryActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
            else{
                val intent = Intent(context, DetailedHistoryActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }


//            val exerciseEntry = exerciseEntryList[position]
//            println("HisItemDebug2::" + exerciseEntry.activityType.toString())
        }
        return view
    }
    // when switch to fragmentHistory, immediately update the data
    override fun onResume()
    {
        super.onResume()
        exerciseEntryAdapter.notifyDataSetChanged()
    }
}