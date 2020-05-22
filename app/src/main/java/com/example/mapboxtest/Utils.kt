package com.example.mapboxtest

import com.mapbox.mapboxsdk.geometry.LatLng


const val FAKE_LOCATION_DELTA = 0.001

fun LatLng.generateFakeLocations(): List<LatLng> = listOf(
    LatLng(
        this.latitude + (FAKE_LOCATION_DELTA * 0),
        this.longitude + (FAKE_LOCATION_DELTA * 1)
    ),
    LatLng(
        this.latitude + (FAKE_LOCATION_DELTA * 1),
        this.longitude + (FAKE_LOCATION_DELTA * 2)
    ),
    LatLng(
        this.latitude + (FAKE_LOCATION_DELTA * 3),
        this.longitude + (FAKE_LOCATION_DELTA * 3)
    )
)