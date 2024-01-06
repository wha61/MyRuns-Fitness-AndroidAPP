package com.example.ActivityRecognition.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ActivityRecognition.ExerciseEntryViewModel
import com.example.ActivityRecognition.ExerciseEntryViewModelFactory
import com.example.ActivityRecognition.LatlngConverter.Latlngs
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.database.ExerciseEntry
import com.example.ActivityRecognition.database.ExerciseEntryDatabase
import com.example.ActivityRecognition.database.ExerciseEntryDatabaseDao
import com.example.ActivityRecognition.database.ExerciseEntryRepository
import com.example.ActivityRecognition.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlin.properties.Delegates

class MapHistoryActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val PERMISSION_REQUEST_CODE = 0
    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: java.util.ArrayList<Polyline>


    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var exerciseEntryViewModel: ExerciseEntryViewModel

    private lateinit var exerciseEntry: ExerciseEntry

    private var id by Delegates.notNull<Long>()
    private var count : Long = 0;

    private lateinit var latlngs : ArrayList<Latlngs>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set up database, only operate on the viewModel layer
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        exerciseEntryViewModel = ViewModelProvider(this, viewModelFactory).get(
            ExerciseEntryViewModel::class.java)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        exerciseEntryViewModel.allExerciseEntryLiveData.observe(this) {
            val exerciseEntryList: ArrayList<ExerciseEntry> = it as ArrayList<ExerciseEntry>
            id = intent.getLongExtra("id", -1)
            println("id is" + id)
            val size = exerciseEntryList.size
            if(id < size){
                exerciseEntry = exerciseEntryList[id.toInt()]
                val textView : TextView = findViewById(R.id.myHistoryLocationText)

                val type : String = intToActivityType(exerciseEntry.activityType)
                val avgSpeed : Double = exerciseEntry.avgSpeed
                val curSpeed : String = "n/a"
                val climb : Double = exerciseEntry.climb
                val cal : Double = exerciseEntry.calorie
                val distance : Double = exerciseEntry.distance
                latlngs = exerciseEntry.locationList
                println("latlngs is after " + latlngs)

                // use SharedPreferences here, change data base on unit
                val sp: SharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE)
                val unitPreferences = sp.getString("UnitPreference", "")
                println("unit::$unitPreferences")

                val avg = "%.2f".format(avgSpeed*0.6/1000)
                val dis = "%.2f".format(distance*0.6/1000)
                val c = "%.2f".format(climb*0.6)
                val cal1 = "%.2f".format(cal)
                textView.text = "Type: $type \nclimb: $c Miles\navg: $avg m/h\ncur: $curSpeed m/h\nCal: $cal1\nDis: $dis Miles\n"

                if(unitPreferences=="Metric (Kilometers)"){
                    val avg = "%.2f".format(avgSpeed/1000)
                    val dis = "%.2f".format(distance/1000)
                    val c = "%.2f".format(climb)
                    val cal = "%.2f".format(cal)
                    textView.text = "Type: $type \nclimb: $c Kilometers\navg: $avg km/h\ncur: $curSpeed km/h\nCal: $cal\nDis: $dis Kilometers\n"
                }

                val markers : java.util.ArrayList<Marker> = java.util.ArrayList()
                val markersOptions : java.util.ArrayList<MarkerOptions> = java.util.ArrayList()

//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

                for (l in latlngs){
                    val lat = l.lat
                    val lng = l.lng
                    val latLng = LatLng(lat, lng)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    mMap.animateCamera(cameraUpdate)

                    val i = markerOptions.position(latLng)
                    markersOptions.add(i)
                    println("map:: markerOption" + markersOptions)

                    val marker = mMap.addMarker(markerOptions)
                    if (marker != null) {
                        markers.add(marker)
                        println("map:: marker " + markers)
                    }

                    polylineOptions.add(latLng)
                    polylines.add(mMap.addPolyline(polylineOptions))


                    val size1 = markers.size
                    if(size1 > 2){
                        markers[size1-2].remove()
                    }
                }


                count = exerciseEntry.id
            }
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//        mMap.setOnMapClickListener(this)
//        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = java.util.ArrayList()
        markerOptions = MarkerOptions()
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