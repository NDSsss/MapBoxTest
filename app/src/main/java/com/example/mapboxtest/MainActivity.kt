package com.example.mapboxtest

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.utils.ColorUtils
import kotlinx.android.synthetic.main.activity_main.*


const val DEFAULT_ZOOM = 15.0
const val MAP_IC_CURRENT_LOCATION = "MAP_IC_CURRENT_LOCATION"

const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
const val CIRCLE_RADIUS = 7F
const val LINE_WIDTH = 3F

class MainActivity : AppCompatActivity(), PermissionsListener {

    private lateinit var permissionsManager: PermissionsManager
    private lateinit var mapBoxMap: MapboxMap
    private lateinit var locationEngine: LocationEngine
    private lateinit var symbolManager: SymbolManager
    private lateinit var circleManager: CircleManager
    private lateinit var lineManager: LineManager
    private var symbol: Symbol? = null
    private var lines: Line? = null
    private val circles: ArrayList<Circle> = arrayListOf()

    private val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult) {
            Log.d("APP_TAG", "onSuccess result ${result.lastLocation}")
            result.lastLocation?.let { location ->
                locationReceived(location)
            }
        }

        override fun onFailure(exception: Exception) {
            Log.d("APP_TAG", "onFailure exception ${exception.localizedMessage}")
            Toast.makeText(this@MainActivity, exception.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        act_main_mv?.onCreate(savedInstanceState)
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)

        act_main_iv_current_location.setOnClickListener {
            if (requestPermissionsIfNotGranted()) {
                locationEngine.getLastLocation(callback)
            }
        }
        if (requestPermissionsIfNotGranted()) {
            permissionsGranted()
        }
    }

    private fun requestPermissionsIfNotGranted(): Boolean =
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            true
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
            false
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {

    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            permissionsGranted()
        } else {
            Toast.makeText(this, R.string.location_permissions_denied, Toast.LENGTH_LONG).show()
        }
    }

    private fun permissionsGranted() {
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
            .setPriority(LocationEngineRequest.PRIORITY_LOW_POWER)
            .build()
        act_main_mv?.getMapAsync { readyMap ->
            mapBoxMap = readyMap
            mapBoxMap.setStyle(
                Style.Builder()
                    .fromUri(Style.MAPBOX_STREETS)
                    .withImage(
                        MAP_IC_CURRENT_LOCATION,
                        ContextCompat.getDrawable(this, R.drawable.mapbox_ic_place)
                            ?: throw IllegalArgumentException("Drawable can't be null"),
                        true
                    )
            ) { style ->
                symbolManager = SymbolManager(act_main_mv, mapBoxMap, style)
                symbolManager.iconAllowOverlap = true
                symbolManager.textAllowOverlap = true
                circleManager = CircleManager(act_main_mv, mapBoxMap, style)
                lineManager = LineManager(act_main_mv, mapBoxMap, style)

                locationEngine.requestLocationUpdates(request, callback, mainLooper)
                locationEngine.getLastLocation(callback)
            }
        }
    }

    private fun locationReceived(location: Location) {
        val foundLatNng = LatLng(
            location.latitude,
            location.longitude
        )
        symbol?.let {
            symbolManager.delete(it)
        }
        symbol = null
        symbol = SymbolOptions()
            .withLatLng(foundLatNng)
            .withIconImage(MAP_IC_CURRENT_LOCATION).let { optons ->
                symbolManager.create(optons)
            }
        val fakeLocations = foundLatNng.generateFakeLocations()
        circles.forEach { circle ->
            circleManager.delete(circle)
        }
        circles.clear()
        fakeLocations.forEach { fakeLocation ->
            circles.add(
                circleManager.create(
                    CircleOptions()
                        .withLatLng(fakeLocation)
                        .withCircleColor(ColorUtils.colorToRgbaString(Color.BLACK))
                        .withCircleRadius(CIRCLE_RADIUS)
                        .withDraggable(false)
                )
            )
        }
        lines?.let {
            lineManager.delete(it)
        }
        lines = lineManager.create(
            LineOptions()
                .withLatLngs(fakeLocations.plus(foundLatNng))
                .withLineColor(ColorUtils.colorToRgbaString(Color.GRAY))
                .withLineWidth(LINE_WIDTH)
        )

        mapBoxMap.animateCamera {
            CameraPosition.Builder()
                .target(foundLatNng)
                .zoom(DEFAULT_ZOOM)
                .build()
        }
    }

    override fun onStart() {
        super.onStart()
        act_main_mv?.onStart()
    }

    override fun onResume() {
        super.onResume()
        act_main_mv?.onResume()
    }

    override fun onPause() {
        super.onPause()
        act_main_mv?.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationEngine.removeLocationUpdates(callback)
        act_main_mv.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        act_main_mv.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        act_main_mv?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        act_main_mv?.onDestroy()
    }
}
