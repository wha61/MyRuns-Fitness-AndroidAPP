package com.example.ActivityRecognition.map

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel(), ServiceConnection{
    private var myMessageHandler: MyMessageHandler = MyMessageHandler(Looper.getMainLooper())

    // Todo: add mutable data here
    // use bundle to transfer data set
    private val _bundle = MutableLiveData<Bundle>()
    val bundle: LiveData<Bundle>
        get() {
            return _bundle
        }

    inner class MyMessageHandler(looper: Looper): Handler(looper){
        override fun handleMessage(msg: Message) {
            if(msg.what == TrackingService.MSG_INT_VALUE){
                _bundle.value = msg.data
                println("map:: viewModel's bundle value passed::" + _bundle.value)
            }
        }
    }

    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        val tempBinder = iBinder as TrackingService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
        println("map:: binder's message handler set. $myMessageHandler")
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        TODO("Not yet implemented")
    }


}