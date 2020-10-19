package com.fdlsoftware.demotracking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TrackerService : Service() {

    companion object {
        const val TAG: String = "TrackerService"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        buildNotification()
        loginToFirebase()
    }

    private fun buildNotification() {
        val stop = "Stop"
        val channelId = "traxpark_default_channel"
        registerReceiver(stopReceiver, IntentFilter(stop))
        val broadcastIntent =
            PendingIntent.getBroadcast(this, 0, Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_text))
            .setOngoing(true)
            .setContentIntent(broadcastIntent)
            .setSmallIcon(R.drawable.ic_android)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                "DemoTracking",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        startForeground(1, builder.build())
    }

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "received stop broadcast")
            stop()
        }
    }

    private fun stop() {
        unregisterReceiver(stopReceiver)
        stopSelf()
    }

    private fun loginToFirebase() {
        val email = getString(R.string.firebase_email)
        val password = getString(R.string.firebase_password)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    Log.i(TAG, "firebase auth success")
                    requestLocationUpdates()
                } else {
                    Log.i(TAG, "firebase auth failed")
                }
            }

    }

    private fun requestLocationUpdates() {

        val locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val client = FusedLocationProviderClient(this)
        val path = getString(R.string.firebase_path) + "/" + getString(R.string.transport_id)
        val permission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult?) {
                    val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference(path)
                    result?.lastLocation.let {
                        Log.d(TAG, "location update $it");
                        ref.setValue(it)
                    }
                }
            }
            client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

    }

}
