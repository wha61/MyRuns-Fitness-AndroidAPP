package com.example.ActivityRecognition.LatlngConverter

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converter {
    @TypeConverter
    fun arrayListToJson(arrayList: ArrayList<Latlngs>?) = Json.encodeToString(arrayList)

    @TypeConverter
    fun jsonToArraylist(json : String) = Json.decodeFromString<ArrayList<Latlngs>>(json)
}