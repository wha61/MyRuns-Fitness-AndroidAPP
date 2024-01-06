package com.example.ActivityRecognition.database

import androidx.room.*
import com.example.ActivityRecognition.LatlngConverter.Converter
import com.example.ActivityRecognition.LatlngConverter.Latlngs

@Entity(tableName = "ExerciseEntry_table")
@TypeConverters(Converter::class)
data class ExerciseEntry (
    //Primary Key
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    // Manual, GPS or automatic
    @ColumnInfo(name = "inputType")
    var inputType: Int = 0,

    // Running, cycling etc.
    @ColumnInfo(name = "activityType")
    var activityType: Int = 0,

    // When does this entry happen
    @ColumnInfo(name = "dateTime")
//    var dateTime: Calendar = Calendar.getInstance(),
    var dateTime: String = "",

    // Exercise duration in seconds
    @ColumnInfo(name = "duration")
    var duration: Double = 0.0,

    // Distance traveled. Either in meters or feet.
    @ColumnInfo(name = "distance")
    var distance: Double = 0.0,

    // Average pace
    @ColumnInfo(name = "avgPace")
    var avgPace: Double = 0.0,

    // Average speed
    @ColumnInfo(name = "avgSpeed")
    var avgSpeed: Double = 0.0,

    // Calories burnt
    @ColumnInfo(name = "calorie")
    var calorie: Double = 0.0,

    // Climb. Either in meters or feet.
    @ColumnInfo(name = "climb")
    var climb: Double = 0.0,

    // Heart rate
    @ColumnInfo(name = "heartRate")
    var heartRate: Double = 0.0,

    // Comments
    @ColumnInfo(name = "comment")
    var comment: String = "",

    // locationList
    @ColumnInfo(name = "locationList")
    var locationList: ArrayList<Latlngs> = ArrayList(),




    )
