package com.rego.regotracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.TextView
import java.io.File

class RegoLocationWriter(val minSaveInterval: Long, val updateInterval: Long, val context: Context) : LocationListener {

    var saveLocation = true

    var filesSinceStart = 0

    val startTime = System.currentTimeMillis()

    var totalDelays = 0L

    var timeOfLastSave = 0L

    var timeBetweenPreviousFiles = 0L

    fun startWritingLocations(locationManager: LocationManager, context: Context) {
        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (finePermission != PackageManager.PERMISSION_GRANTED || coarsePermission != PackageManager.PERMISSION_GRANTED) {
            println("Error: no perms")
            println("fine: $finePermission")
            println("coarse: $coarsePermission")
        } else {
            //locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateInterval, 0f, this)
        }
    }

    fun writeLocationFile(location: Location) {
        val rLoc = RegoLocation(
            location.latitude,
            location.longitude,
            location.accuracy,
            location.altitude,
            location.speed,
            location.time
        )

        val path = context.filesDir

        val fileName = "${rLoc.time}.json"
        val file = File(path, fileName)

        println("wrting file: ${file.name}")
        file.writeText(rLoc.toJsonString())
    }

    override fun onLocationChanged(location: Location?) {
        val timeSinceLast = System.currentTimeMillis() - timeOfLastSave
        println("time since in millis: $timeSinceLast")
        if (timeSinceLast > minSaveInterval) {
            if (location != null) {
                if (timeOfLastSave != 0L) {
                    totalDelays += timeSinceLast - minSaveInterval
                }
                timeBetweenPreviousFiles = timeSinceLast
                timeOfLastSave = System.currentTimeMillis()
                filesSinceStart++
                writeLocationFile(location)
            }
        }
        println(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }


}