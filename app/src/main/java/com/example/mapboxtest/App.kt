package com.example.mapboxtest

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(applicationContext,getString(R.string.map_box_key))
    }
}