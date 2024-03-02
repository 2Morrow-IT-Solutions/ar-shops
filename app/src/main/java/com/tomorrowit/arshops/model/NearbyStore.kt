package com.tomorrowit.arshops.model

import com.google.android.gms.maps.model.LatLng
import com.google.ar.sceneform.math.Vector3
import com.google.gson.annotations.SerializedName
import com.google.maps.android.ktx.utils.sphericalHeading
import java.io.Serializable
import kotlin.math.cos
import kotlin.math.sin

data class NearbyStore(
    @SerializedName("place_id")
    var place_id: String = "",

    @SerializedName("name")
    var name: String = "",

    @SerializedName("locationlat")
    var locationlat: Float = 0f,

    @SerializedName("locationlng")
    var locationlng: Float = 0f,

    @SerializedName("address")
    var address: String = "",

    @SerializedName("type")
    var type: String = "",

    @SerializedName("businessStatus")
    var businessStatus: String = "",

    @SerializedName("distance")
    var distance: Int = 0,

    @SerializedName("photo_reference")
    var photo_reference: String = "",

    @SerializedName("open_now")
    var open_now: Boolean = false,

    @SerializedName("registered")
    var registered: Boolean = false
) : Serializable

fun NearbyStore.getPositionVector(azimuth: Float, latLng: LatLng): Vector3 {

    val placeLatLng =
        GeometryLocation(this.locationlat.toDouble(), this.locationlng.toDouble()).latLng
    val heading = latLng.sphericalHeading(placeLatLng)
    val r = -2f
    val x = r * sin(azimuth + heading).toFloat()
    val y = 2f
    val z = r * cos(azimuth + heading).toFloat()
    return Vector3(x, y, z)
}

//data class Geometry(
//    val location: GeometryLocation
//)

data class GeometryLocation(
    val lat: Double,
    val lng: Double
) {
    val latLng: LatLng
        get() = LatLng(lat, lng)
}