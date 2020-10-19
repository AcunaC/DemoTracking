package com.fdlsoftware.demotracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class TrackerActivity : AppCompatActivity() {

    companion object {
        const val PERMISSIONS_REQUEST = 1
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST
            && grantResults.isNotEmpty()
            && grantResults.first() == PackageManager.PERMISSION_GRANTED
        ) {
            startTrackerService()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
            finish()
        }

        val permission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST
            )
        }

        setContentView(R.layout.activity_tracker)
    }

    private fun startTrackerService() {
        startService(Intent(this, TrackerService::class.java))
        finish()
    }
}