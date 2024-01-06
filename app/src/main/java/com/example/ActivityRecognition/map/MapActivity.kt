package com.example.ActivityRecognition.map


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.ActivityRecognition.ExerciseEntryViewModel
import com.example.ActivityRecognition.ExerciseEntryViewModelFactory
import com.example.ActivityRecognition.LatlngConverter.Latlngs
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.Util
import com.example.ActivityRecognition.database.ExerciseEntry
import com.example.ActivityRecognition.database.ExerciseEntryDatabase
import com.example.ActivityRecognition.database.ExerciseEntryDatabaseDao
import com.example.ActivityRecognition.database.ExerciseEntryRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.time.LocalDateTime
import java.util.*
import kotlin.properties.Delegates


class MapActivity : AppCompatActivity(), OnMapReadyCallback
//    , GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener
{

    private lateinit var mMap: GoogleMap
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>

    private var lat by Delegates.notNull<Double>()
    private var lng by Delegates.notNull<Double>()
    private var line3 by Delegates.notNull<Double>()
    private var line4 by Delegates.notNull<Float>()
    private var line5 by Delegates.notNull<Float>()
    private var line6 by Delegates.notNull<Int>()
    private var type : String = "Unknown"
    private lateinit var inputType : String

    private var avgSpeed by Delegates.notNull<Double>()
    private var calorie by Delegates.notNull<Float>()
    private var timePeriod by Delegates.notNull<Int>()


    private lateinit var textView: TextView

    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var serviceIntent: Intent
    private lateinit var mapViewModel: MapViewModel

    private var isBind = false
    private lateinit var appContext: Context

    private lateinit var startTime : LocalDateTime

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var exerciseEntryViewModel: ExerciseEntryViewModel

    private var dataSize by Delegates.notNull<Int>()

    private lateinit var LatLngs : ArrayList<Latlngs>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        title="MyRuns4-Map"

        // before start the service, first check whether have the permission
        // for the first time when download the app
        Util.checkLocPermission(this)
        // if no permission, then return to start activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){
            finish()
        }

        inputType  = intent.getStringExtra("inputType").toString()

        startTime = LocalDateTime.now()

        // define serviceIntent here, start services after get location permission
        serviceIntent = Intent(this, TrackingService::class.java)
        startService(serviceIntent)
        println("test: Service start")
        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        appContext = this.applicationContext

        textView = findViewById(R.id.myLocationText)

        lat =0.0
        lng =0.0
        line3 =0.0
        line4 =0f
        line5 = 0f
        line6 = 0

        LatLngs = ArrayList()

        // here check permission again, if get permission then start the service, bind service
        checkPermission()


        // set up database, only operate on the viewModel layer
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        exerciseEntryViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseEntryViewModel::class.java)

        saveButton = findViewById(R.id.mapSaveButtons)
        cancelButton = findViewById(R.id.mapCancelButtons)

        saveButton.setOnClickListener() {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
            // Todo: save data into database, data: activityType, avgSpeed, curSpeed(N/A), climb, calorie, distance, duration

            dataSize = intent.getIntExtra("dataSize", -1)
            val inputType : String? = intent.getStringExtra("inputType")
            println("input type: " + inputType)
            val activityType : String? = intent.getStringExtra("activityType")

            // save data to Entity
            val exerciseEntry = ExerciseEntry()

            val tempCalendar : Calendar = Calendar.getInstance()

            val year = tempCalendar.get(Calendar.YEAR)
            val month = tempCalendar.get(Calendar.MONTH) + 1
            val day = tempCalendar.get(Calendar.DAY_OF_MONTH)
            val date = "$month-$day-$year"

            val hour = tempCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = tempCalendar.get(Calendar.MINUTE)
            val second = tempCalendar.get(Calendar.SECOND)
            val time = "$hour:$minute:$second"
            // here id : primary key is automated generated
//            exerciseEntry.id = inputId.toLong()



            exerciseEntry.inputType = inputTypeToInt(inputType)
            if(inputType == "GPS Entry"){
                exerciseEntry.activityType = activityTypeToInt(activityType)
            }
            else{
                exerciseEntry.activityType = activityTypeToInt(type)
            }
            exerciseEntry.dateTime = "$time $date"

            exerciseEntry.duration = line6.toDouble()
            exerciseEntry.avgSpeed = avgSpeed
            exerciseEntry.climb = line3
            exerciseEntry.calorie = calorie.toDouble()
            exerciseEntry.distance = line4.toDouble()
            exerciseEntry.locationList = LatLngs



            // insert entity to database through viewmodel
            exerciseEntryViewModel.insert(exerciseEntry)

            unBindService()
//            applicationContext.unbindService(mapViewModel)
            stopService(serviceIntent)
            Toast.makeText(this, "$dataSize $inputType $activityType", Toast.LENGTH_SHORT).show()
            finish()
        }

        cancelButton.setOnClickListener() {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
            unBindService()
//            applicationContext.unbindService(mapViewModel)
            stopService(serviceIntent)
            Toast.makeText(this, "Entry discarded", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    private fun bindService(){
        if (!isBind) {
            // BIND_AUTO_CREATE, if no service is running, first start the services and bind it
            appContext.bindService(serviceIntent, mapViewModel, Context.BIND_AUTO_CREATE)
            println("test: m services bound")
            isBind = true
        }
    }

    private fun unBindService(){
        if (isBind) {
            appContext.unbindService(mapViewModel)
            isBind = false
            println("test: m service unbinded")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("test: map onDestroyed")
        stopService(serviceIntent)
//        applicationContext.unbindService(mapViewModel)

    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION), 0)
        else{

            bindService()
            println("test: Service bind")
            startTime = LocalDateTime.now()
        }


    }


    @SuppressLint("SetTextI18n")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//        mMap.setOnMapClickListener(this)
//        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()

        val markers : ArrayList<Marker> = ArrayList()
        val markersOptions : ArrayList<MarkerOptions> = ArrayList()

        // get start map activity and bind service time (initial time)
        println("debug:: time " + startTime.hour + startTime.minute + startTime.second )

        val startTimeInSeconds : Int =  (startTime.hour)*3600 + (startTime.minute)*60 + (startTime.second)*1

        println("debug:: time " + startTimeInSeconds)

        val activityType : String? = intent.getStringExtra("activityType")

        if(inputType == "Automatic Entry"){
            textView.text = "Type: $type \nclimb: 0.00 Miles\navg: 0.00 m/h\ncur: 0.00 m/h\nCal: 0.00\nDis: 0.00 Miles\ntime: 0 Seconds"
        }

        mapViewModel.bundle.observe(this) {

            type = it.getString("Type", "")

            lat = it.getDouble("lat")
            lng = it.getDouble("lng")
            line3 = it.getDouble("climb")
            line4 = it.getFloat("distance")
            line5 = it.getFloat("curSpeed")

            line6 = it.getInt("duration")


            val currentTime = LocalDateTime.now()
            val currentTimeInSeconds : Int =  (currentTime.hour)*3600 + (currentTime.minute)*60 + (currentTime.second)*1
            println("debug:: time" + currentTimeInSeconds)

            // here timePeriod is from start map to now, use to calculate speed, in hour
            timePeriod = (currentTimeInSeconds - startTimeInSeconds)
            println("debug: speed timePeriod " + timePeriod )

            println("debug: speed accumulateDistance" + line4)

            avgSpeed = (line4/timePeriod)*3.6

            var distance = line4
            if(distance != 0f){
                distance/=1000
            }

            calorie = (line4/1000)*20
            println("loc:: observe " + lat + lng)

            // use SharedPreferences here, change data base on unit
            val sp: SharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE)
            val unitPreferences = sp.getString("UnitPreference", "")
            println("unit::$unitPreferences")

            val avg = "%.2f".format(avgSpeed*0.6)
            val dis = "%.2f".format(distance*0.6)
            val c = "%.2f".format(line3*0.6)
            val cur = "%.2f".format(line5*0.6)
            val cal = "%.2f".format(calorie)
            if(inputType == "GPS Entry"){
                textView.text = "Type: $activityType \nclimb: $c Miles\navg: $avg m/h\ncur: $cur m/h\nCal: $cal\nDis: $dis Miles\ntime: $line6 Seconds"
            }
            else{
                textView.text = "Type: $type \nclimb: $c Miles\navg: $avg m/h\ncur: $cur m/h\nCal: $cal\nDis: $dis Miles\ntime: $line6 Seconds"
            }


            if(unitPreferences=="Metric (Kilometers)"){
                val avg = "%.2f".format(avgSpeed)
                val dis = "%.2f".format(distance)
                val c = "%.2f".format(line3)
                val cur = "%.2f".format(line5)
                val cal = "%.2f".format(calorie)
                if(inputType == "GPS Entry"){
                    textView.text = "Type: $activityType \nclimb: $c Miles\navg: $avg m/h\ncur: $cur m/h\nCal: $cal\nDis: $dis Miles\ntime: $line6 Seconds"
                }
                else{
                    textView.text = "Type: $type \nclimb: $c Miles\navg: $avg m/h\ncur: $cur m/h\nCal: $cal\nDis: $dis Miles\ntime: $line6 Seconds"
                }
            }

            LatLngs.add(Latlngs(lat, lng))
            println("latlngs is " + LatLngs)

            val latLng = LatLng(lat, lng)

//            if (!mapCentered) {
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

    }



    // as inputType in database is saved as Int, convert from String to Int
    // Todo: currently only implement the "manual entry part" "GPS" as need, add Automatic later
    fun inputTypeToInt(inputType: String?) : Int{
        if(inputType == "Manual Entry") {
            return 1
        }
        else if(inputType == "GPS Entry"){
            return 2
        }
        else{
            return 3
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