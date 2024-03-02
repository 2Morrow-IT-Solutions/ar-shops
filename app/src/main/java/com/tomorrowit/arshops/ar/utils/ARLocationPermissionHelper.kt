package com.tomorrowit.arshops.ar.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object ARLocationPermissionHelper {

    @JvmStatic
    private val PERMISSION_CODE = 0

    @JvmStatic
    private val CAMERA_PERMISSION = Manifest.permission.CAMERA

    @JvmStatic
    private val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

    /**
     * Check to see we have the necessary permissions for this app.
     */
    @JvmStatic
    fun hasPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            activity,
            LOCATION_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check to see we have the necessary permissions for this app, and ask for them if we don't.
     */
    @JvmStatic
    fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(CAMERA_PERMISSION, LOCATION_PERMISSION),
            PERMISSION_CODE
        )
    }

    /** Check to see if we need to show the rationale for this permission.  */
    @JvmStatic
    fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            CAMERA_PERMISSION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            activity, LOCATION_PERMISSION
        )
    }

    /** Launch Application Setting to grant permission.  */
    @JvmStatic
    fun launchPermissionSettings(activity: Activity) {
        val intent = Intent()
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.setData(Uri.fromParts("package", activity.packageName, null))
        activity.startActivity(intent)
    }
}