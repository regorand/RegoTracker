package com.rego.regotracker

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?


        val scheduler = Executors.newSingleThreadScheduledExecutor()

        val regoLocationWriter = RegoLocationWriter(60000, 5000, this)

        if (ContextCompat.checkSelfPermission(this, Context.LOCATION_SERVICE)
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, LOCATION_SERVICE),
                1
            )
        }

        val button = findViewById<Button>(R.id.refresh)

        button.setOnClickListener {
            updateNumberOfFiles(regoLocationWriter)
        }

        scheduler.scheduleAtFixedRate({
            updateNumberOfFiles(regoLocationWriter)
        }, 0, 1, TimeUnit.SECONDS)


        //TODO problem is probably, that this is run in same thread as MainActivity, which gets suspended when screen changes or goes dark
        //TODO find out how to start this in background thread
        regoLocationWriter.startWritingLocations(locationManager!!, this)

        /*
        val finePermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
        val coarsePermission = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
        val locPermission = ContextCompat.checkSelfPermission(this, LOCATION_SERVICE)
        if (finePermission != PackageManager.PERMISSION_GRANTED || coarsePermission != PackageManager.PERMISSION_GRANTED || locPermission != PackageManager.PERMISSION_GRANTED) {
            println("Error: no perms")
            println("fine: $finePermission")
            println("coarse: $coarsePermission")
            println("loc: $locPermission")
        } else {
/*
            val timer = Timer()



            timer.scheduleAtFixedRate(object: TimerTask() {
                override fun run() {
                    if (ContextCompat.checkSelfPermission(listener, Context.LOCATION_SERVICE) == PackageManager.PERMISSION_GRANTED) {
                        locationManager?.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
                    }
                }
            }, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1))
            */
            val listener = this
            val scheduler = Executors.newSingleThreadScheduledExecutor()


            println("scheduling")

            scheduler.scheduleAtFixedRate({
                println("scheduled")
                if (ContextCompat.checkSelfPermission(this, Context.LOCATION_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {

                    println("Error: no perms 1")
                }

                val finePermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                val coarsePermission = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                if (finePermission != PackageManager.PERMISSION_GRANTED || coarsePermission != PackageManager.PERMISSION_GRANTED) {
                    println("Error: no perms 2")
                }
                locationManager?.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
            }, 0, 1, TimeUnit.MINUTES)
        }
        */
    }

    private fun updateNumberOfFiles(regoLocationWriter: RegoLocationWriter) {
        val fileList = fileList()
        val timeSinceLast = System.currentTimeMillis() - regoLocationWriter.timeOfLastSave
        val fileAmt = regoLocationWriter.filesSinceStart
        var fileRate = 0.0
        val lastGap = regoLocationWriter.timeBetweenPreviousFiles
        val totalDelay = regoLocationWriter.totalDelays
        if (fileAmt > 0) {
            fileRate = (System.currentTimeMillis() - regoLocationWriter.startTime).toDouble() / fileAmt
        }
        val textView = findViewById<TextView>(R.id.status)

        val str = "Number of Files: ${fileList.size}\n" +
                "Last Save: $timeSinceLast\n" +
                "Files written since start: $fileAmt\n" +
                "File Rate: $fileRate\n" +
                "time gap between last saves: $lastGap\n" +
                "total delay beyond plan: $totalDelay"

        textView.text = str
    }

}
