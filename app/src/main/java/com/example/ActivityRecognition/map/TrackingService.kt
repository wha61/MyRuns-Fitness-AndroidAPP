package com.example.ActivityRecognition.map

import android.app.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.*
import android.os.*
import android.provider.CalendarContract
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.classifier.FFT
import com.example.ActivityRecognition.classifier.Globals
import com.example.ActivityRecognition.classifier.WekaClassifier
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.security.KeyStore
import java.time.LocalDateTime
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

import kotlin.properties.Delegates

import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instance
import weka.core.Instances
import weka.core.converters.ArffSaver
import weka.core.converters.ConverterUtils
import java.text.DecimalFormat

class TrackingService : Service(), LocationListener, SensorEventListener {
    // add notification
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 666
    private val CHANNEL_ID = "notification channel"

    // binder
    private lateinit var myBinder: MyBinder
    private var msgHandler: Handler? = null
    companion object{
        val INT_KEY = "int key"
        val MSG_INT_VALUE = 0
    }

    // use location manager to get location data
    private lateinit var locationManager: LocationManager

    // initial data when start a GPS mode
    private var startTime by Delegates.notNull<Double>()
    private lateinit var startLocation : Location

    private lateinit var locations : ArrayList<Location>
    private lateinit var times : ArrayList<Int>
    private lateinit var altitudes : ArrayList<Double>
    private lateinit var LatLngs : ArrayList<LatLng>


    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private lateinit var mDataset: Instances
    private lateinit var mClassAttribute: Attribute
    private lateinit var mAsyncTask: OnSensorChangedTask
    private lateinit var mAccBuffer: ArrayBlockingQueue<Double>
    private lateinit var featureVector : ArrayBlockingQueue<Double>

    private var type : String = "Standing"

    inner class MyBinder : Binder() {
        fun setmsgHandler(mapMsgHandler: Handler) {
            this@TrackingService.msgHandler = mapMsgHandler
            println("loc::3 " + msgHandler)
        }
    }

    override fun onCreate() {
        super.onCreate()

        myBinder = MyBinder()

        locations = ArrayList()
        times = ArrayList()
        altitudes  = ArrayList()
        LatLngs = ArrayList()

        TimeUnit.SECONDS.sleep(2)

        initLocationManager()

        showNotification()

        mAccBuffer = ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY)
        featureVector = ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY)

        println("loc::1 " + msgHandler)
        println("test: Service create")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)

        mAsyncTask = OnSensorChangedTask()
        mAsyncTask.execute()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        println("test: Service onBind() called")
        return myBinder

    }

    override fun onUnbind(intent: Intent?): Boolean {
        msgHandler = null
        println("test: Service onUnbind")
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupTasks()
        stopSelf()
        println("test: Service onDestroy")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cleanupTasks()
        stopSelf()
        println("test: app removed from the application list")
    }

    private fun cleanupTasks(){
//        msgHandler = null
        notificationManager.cancel(NOTIFICATION_ID)
        locationManager.removeUpdates(this)

        mAsyncTask.cancel(true)
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        mSensorManager.unregisterListener(this)
        Log.i("", "")
        println("test: cleanupTasks")
    }


    private fun showNotification(){
        // set the intent when click the notification, go to map activity
        val intent = Intent(this, MapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE)

        // set notification builder
        val notificationBuilder : NotificationCompat.Builder = NotificationCompat.Builder(this,CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.eye)
        notificationBuilder.setContentTitle("MyRuns")
        notificationBuilder.setContentText("Recording your path now")
        notificationBuilder.setContentIntent(pendingIntent)

        val notification = notificationBuilder.build()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun initLocationManager() {
        try {
                // get location manager
                locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                // set up criteria
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_FINE
                val provider: String? = locationManager.getBestProvider(criteria, true)
                if(provider != null){
                    // move this line up here
                    // change location repeatedly, add location listener at top
                    locationManager.requestLocationUpdates(provider, 0, 0f, this)
                    val location = locationManager.getLastKnownLocation(provider)
                    if(location != null){
                        // update location to views
                        onLocationChanged(location)
                        println("test: onLocChange run")
                    }
//                    // change location repeatedly, add location listener at top
//                    locationManager.requestLocationUpdates(provider, 0, 0f, this)
                    println("test: listener run")
                }
             }
        catch (e: SecurityException){
        }
    }

    // pass data to map activity through bundle
    override fun onLocationChanged(location: Location) {
        try {
            locations.add(location)
            println("debug: locations " + locations.indices)

            val currentTime = LocalDateTime.now()
            val currentTimeInSeconds : Int =  (currentTime.hour)*3600 + (currentTime.minute)*60 + (currentTime.second)*1
            times.add(currentTimeInSeconds)

            // get total distance
            // about how to get distance, use distanceTo
            // reference: https://stackoverflow.com/questions/28209548/android-how-to-use-location-distanceto
            var accumulateDistance : Float = 0F;
            for(index in locations.indices){
                if((index+1) < (locations.size)){
                    val loc = locations[index]
                    val locNext = locations[index+1]
                    accumulateDistance += locNext.distanceTo(loc)
                }
                else{
                    break
                }
            }
//            // in km
//            accumulateDistance /= 1000F
            println("debug: locations distance " + accumulateDistance )


            // get current speed
//            // in km
//            var currentDistancePeriod : Float = (locations[locations.size-1]).distanceTo((locations[locations.size-2]))/1000F
            var currentDistancePeriod : Float = (locations[locations.size-1]).distanceTo((locations[locations.size-2]))
            println("debug: speed currentDistancePeriod " + currentDistancePeriod )
            // in sec -> hour
            var currentTimePeriod = (times[times.size-1] - times[times.size-2])
            println("debug: speed currentTimePeriod " + currentTimePeriod )
            var currentSpeed = (currentDistancePeriod/currentTimePeriod)*3.6f


            var duration = times[times.size-1] - times[0]

            // Todo: get avgSpeed, curSpeed, Climb, Calorie, Distance here
            val lat = location.latitude
            val lng = location.longitude

            val latLng = LatLng(lat, lng)

            LatLngs.add(latLng)

            // get climb
            val altitude = location.altitude
            altitudes.add(altitude)
            var climb = altitudes[altitudes.size-1] - altitudes[0]


            println("loc:: " + lat + lng)

            val bundle = Bundle()
            bundle.putDouble("lat", lat)
            bundle.putDouble("lng", lng)
            bundle.putDouble("climb", climb)
            bundle.putFloat("distance", accumulateDistance)
            bundle.putFloat("curSpeed", currentSpeed)

            bundle.putInt("duration", duration)

            val Reco_Type = type
            bundle.putString("Type", Reco_Type)

            println("loc::4 " + msgHandler)

            println("test: data into bundle")

            if(msgHandler!=null){
                val message = msgHandler!!.obtainMessage()
                message.data=bundle
                message.what = MSG_INT_VALUE
                msgHandler!!.sendMessage(message)
                println("loc::1 message send")
                println("test: data sent")
            }

        } catch (e: Exception) {
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                val m = Math.sqrt((event.values[0] * event.values[0] + event.values[1] * event.values[1] + (event.values[2]
                        * event.values[2])).toDouble())

                // Inserts the specified element into this queue if it is possible
                // to do so immediately without violating capacity restrictions,
                // returning true upon success and throwing an IllegalStateException
                // if no space is currently available. When using a
                // capacity-restricted queue, it is generally preferable to use
                // offer.
                try {
                    mAccBuffer.add(m)
                } catch (e: IllegalStateException) {

                    // Exception happens when reach the capacity.
                    // Doubling the buffer. ListBlockingQueue has no such issue,
                    // But generally has worse performance
                    val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
                    mAccBuffer.drainTo(newBuf)
                    mAccBuffer = newBuf
                    mAccBuffer.add(m)
                }
            }
        }
    }

    inner class OnSensorChangedTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg arg0: Void?): Void? {

            var blockSize = 0
            val fft = FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val accBlock = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            var max = Double.MIN_VALUE
            println(" sensor is running")
            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled() == true) {
                        return null
                    }

                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().toDouble()
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0

                        // time = System.currentTimeMillis();
                        max = .0
                        for (`val` in accBlock) {
                            if (max < `val`) {
                                max = `val`
                            }
                        }
                        fft.fft(accBlock, im)
                        for (i in accBlock.indices) {
                            val mag = Math.sqrt(accBlock[i] * accBlock[i] + im[i]
                                    * im[i])
                            featureVector.add(mag)
                            im[i] = .0 // Clear the field
                        }
//                         Append max after frequency component
                        featureVector.add(max)

                        //Use Weka function to classify and send message
                        val feat = featureVector.toArray()
                        val classifiedVal = WekaClassifier.classify(feat)
                        println("Type is :: " + classifiedVal)

                        type = "Standing"
                        type = if(classifiedVal == 0.0){
                            "Standing"
                        } else if(classifiedVal == 1.0){
                            "Walking"
                        } else if(classifiedVal == 2.0){
                            "Running"
                        } else{
                            "Others"
                        }

                        if(featureVector.size >= 64){
                            featureVector = ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY)
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }


}