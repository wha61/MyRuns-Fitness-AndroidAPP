package com.example.ActivityRecognition.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.database.ExerciseEntry

class FragmentHISTORYListAdapter (private val context: Context, private var exerciseEntryList: List<ExerciseEntry>) : BaseAdapter() {
    override fun getCount(): Int {
        return exerciseEntryList.size
    }

    override fun getItem(p0: Int): Any {
        return exerciseEntryList.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    // set view for each items show in the history list
    @SuppressLint("SetTextI18n", "ResourceType")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.fragment_history_layout_adapter,null)

        val textViewBolded = view.findViewById(R.id.bolded) as TextView
        val textViewNormal = view.findViewById(R.id.normal) as TextView

        val exerciseEntry = exerciseEntryList[position]

        val inputType = intToInputType(exerciseEntry.inputType)

        val activityType = intToActivityType(exerciseEntry.activityType)
        val dateTime = exerciseEntry.dateTime
        textViewBolded.text = "$inputType: $activityType,\n$dateTime"

        // Done: add unit preference here
        // use SharedPreferences here, change data base on unit
        val sp: SharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE)
        val unitPreferences = sp.getString("UnitPreference", "")

        val duration = exerciseEntry.duration
        val distance = exerciseEntry.distance

        if(inputType == "Manual Entry"){
            textViewNormal.text = "$distance Miles, $duration Secs"
            if(unitPreferences=="Metric (Kilometers)"){
                val dis = "%.2f".format(distance*1.6)
                textViewNormal.text = "$dis Kilometers, $duration Secs"
            }
        }
        else{
            val dis = "%.2f".format(distance*0.6/1000)
            textViewNormal.text = "$dis Miles, $duration Secs"
            if(unitPreferences=="Metric (Kilometers)"){
                val disM = "%.2f".format(distance/1000)
                textViewNormal.text = "$disM Kilometers, $duration Secs"
            }
        }

        // change data base on unit
        // default as miles

//        if(unitPreferences=="Imperial (Miles)"){
//            val dis = distance/1.6
//            textViewNormal.text = "$dis Miles, $duration Secs"
//        }
        return view
    }
    // as inputType in database is saved as Int, convert from Int to String
    // Todo: currently only implement the "manual entry part" and "GPS", add GPS and Automatic later
    fun intToInputType(i : Int) : String{
        if(i == 1){
            return "Manual Entry"
        }
        else if(i == 2){
            return "GPS"
        }
        else{
            return "Automatic"
        }
    }
    // as activityType in database is saved as Int, convert from Int to String
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
            return "Other"
        }
    }

    fun replace(newExerciseEntryList: List<ExerciseEntry>){
        exerciseEntryList = newExerciseEntryList
    }

}