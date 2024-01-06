package com.example.ActivityRecognition.LatlngConverter

import kotlinx.serialization.Serializable

@Serializable
data class Latlngs (val lat : Double, val lng : Double){
    private var Lat = lat
    private var Lng = lng
}