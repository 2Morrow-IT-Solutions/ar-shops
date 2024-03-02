package com.tomorrowit.arshops.ar.sensor

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.tomorrowit.arshops.ar.LocationScene
import com.tomorrowit.arshops.ar.utils.KalmanLatLong


class DeviceLocation(context: Context, private val locationScene: LocationScene) :
    LocationListener {
    var currentBestLocation: Location? = null
    private var isLocationManagerUpdatingLocation = false
    private val locationList: ArrayList<Location>
    private val oldLocationList: ArrayList<Location>
    private val noAccuracyLocationList: ArrayList<Location>
    private val inaccurateLocationList: ArrayList<Location>
    private val kalmanNGLocationList: ArrayList<Location>
    private var currentSpeed = 0.0f // meters/second
    private var kalmanFilter: KalmanLatLong
    private var gpsCount = 0
    private var runStartTimeInMillis: Long = 0
    private val locationManager: LocationManager? = null
    var minimumAccuracy = 25
    private val context: Context

    init {
        this.context = context.applicationContext
        locationList = ArrayList()
        noAccuracyLocationList = ArrayList()
        oldLocationList = ArrayList()
        inaccurateLocationList = ArrayList()
        kalmanNGLocationList = ArrayList()
        kalmanFilter = KalmanLatLong(3f)
        startUpdatingLocation()
    }

    override fun onLocationChanged(newLocation: Location) {
        Log.d(TAG, "(" + newLocation.latitude + "," + newLocation.longitude + ")")
        gpsCount++
        filterAndAddLocation(newLocation)
    }

    override fun onProviderDisabled(provider: String) {
        startUpdatingLocation()
    }

    override fun onProviderEnabled(provider: String) {
        try {
            locationManager!!.requestLocationUpdates(provider, 0, 0f, this)
        } catch (e: SecurityException) {
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    fun startUpdatingLocation() {
        if (isLocationManagerUpdatingLocation == false) {
            isLocationManagerUpdatingLocation = true
            runStartTimeInMillis = (SystemClock.elapsedRealtimeNanos() / 1000000)
            locationList.clear()
            oldLocationList.clear()
            noAccuracyLocationList.clear()
            inaccurateLocationList.clear()
            kalmanNGLocationList.clear()
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            //Exception thrown when GPS or Network provider were not available on the user's device.
            try {
                val criteria = Criteria()
                criteria.accuracy =
                    Criteria.ACCURACY_FINE //setAccuracyは内部では、https://stackoverflow.com/a/17874592/1709287の用にHorizontalAccuracyの設定に変換されている。
                criteria.powerRequirement = Criteria.POWER_HIGH
                criteria.isAltitudeRequired = false
                criteria.isSpeedRequired = true
                criteria.isCostAllowed = true
                criteria.isBearingRequired = false

                //API level 9 and up
                criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
                criteria.verticalAccuracy = Criteria.ACCURACY_HIGH
                //criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
                //criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
                val gpsFreqInMillis = 5000
                val gpsFreqInDistance = 1 // in meters

                //locationManager.addGpsStatusListener(this);
                locationManager.requestLocationUpdates(
                    gpsFreqInMillis.toLong(),
                    gpsFreqInDistance.toFloat(),
                    criteria,
                    this,
                    null
                )

                /* Battery Consumption Measurement */gpsCount = 0
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, e.localizedMessage)
            } catch (e: SecurityException) {
                Log.e(TAG, e.localizedMessage)
            } catch (e: RuntimeException) {
                Log.e(TAG, e.localizedMessage)
            }
        }
    }

    private fun stopUpdatingLocation() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(this)
        isLocationManagerUpdatingLocation = false
    }

    private fun getLocationAge(newLocation: Location): Long {
        val locationAge: Long
        locationAge = if (Build.VERSION.SDK_INT >= 17) {
            (SystemClock.elapsedRealtimeNanos() / 1000000) - (newLocation.elapsedRealtimeNanos / 1000000)
        } else {
            System.currentTimeMillis() - newLocation.time
        }
        return locationAge
    }

    private fun filterAndAddLocation(location: Location): Boolean {
        if (currentBestLocation == null) {
            currentBestLocation = location
            locationEvents()
        }
        val age = getLocationAge(location)
        if (age > 5 * 1000) { //more than 5 seconds
            Log.d(TAG, "Location is old")
            oldLocationList.add(location)
            if (locationScene.isDebugEnabled) Toast.makeText(
                context,
                "Rejected: old",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (location.accuracy <= 0) {
            Log.d(TAG, "Latitidue and longitude values are invalid.")
            if (locationScene.isDebugEnabled) Toast.makeText(
                context,
                "Rejected: invalid",
                Toast.LENGTH_SHORT
            ).show()
            noAccuracyLocationList.add(location)
            return false
        }

        //setAccuracy(newLocation.getAccuracy());
        val horizontalAccuracy = location.accuracy
        if (horizontalAccuracy > minimumAccuracy) { //10meter filter
            Log.d(TAG, "Accuracy is too low.")
            inaccurateLocationList.add(location)
            if (locationScene.isDebugEnabled) Toast.makeText(
                context,
                "Rejected: innacurate",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }


        /* Kalman Filter */
        val Qvalue: Float
        val elapsedTimeInMillis = (location.elapsedRealtimeNanos / 1000000) - runStartTimeInMillis
        Qvalue = if (currentSpeed == 0.0f) {
            3.0f //3 meters per second
        } else {
            currentSpeed // meters per second
        }
        kalmanFilter.Process(
            location.latitude,
            location.longitude,
            location.accuracy,
            elapsedTimeInMillis,
            Qvalue
        )
        val predictedLat = kalmanFilter.get_lat()
        val predictedLng = kalmanFilter.get_lng()
        val predictedLocation = Location("") //provider name is unecessary
        predictedLocation.latitude = predictedLat //your coords of course
        predictedLocation.longitude = predictedLng
        val predictedDeltaInMeters = predictedLocation.distanceTo(location)
        if (predictedDeltaInMeters > 60) {
            Log.d(TAG, "Kalman Filter detects mal GPS, we should probably remove this from track")
            kalmanFilter.consecutiveRejectCount += 1
            if (kalmanFilter.consecutiveRejectCount > 3) {
                kalmanFilter =
                    KalmanLatLong(3f) //reset Kalman Filter if it rejects more than 3 times in raw.
            }
            kalmanNGLocationList.add(location)
            if (locationScene.isDebugEnabled) Toast.makeText(
                context,
                "Rejected: kalman filter",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else {
            kalmanFilter.consecutiveRejectCount = 0
        }
        Log.d(TAG, "Location quality is good enough.")
        currentBestLocation = predictedLocation
        currentSpeed = location.speed
        locationList.add(location)
        locationEvents()
        return true
    }

    fun locationEvents() {
        if (locationScene.locationChangedEvent != null) {
            locationScene.locationChangedEvent!!.onChange(currentBestLocation)
        }
        if (locationScene.refreshAnchorsAsLocationChanges()) {
            locationScene.refreshAnchors()
        }
    }

    fun pause() {
        stopUpdatingLocation()
    }

    fun resume() {
        startUpdatingLocation()
    }

    companion object {
        private val TAG = DeviceLocation::class.java.simpleName
        private const val TWO_MINUTES = 1000 * 60 * 2
    }
}