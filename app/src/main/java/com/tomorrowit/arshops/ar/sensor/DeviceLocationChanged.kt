package com.tomorrowit.arshops.ar.sensor

import android.location.Location


interface DeviceLocationChanged {
    fun onChange(location: Location?)
}