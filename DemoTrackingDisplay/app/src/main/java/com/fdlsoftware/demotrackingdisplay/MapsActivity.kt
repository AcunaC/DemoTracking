package com.fdlsoftware.demotrackingdisplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "MapsActivity"

    private lateinit var mMap: GoogleMap
    private val markers: HashMap<String, Marker> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMaxZoomPreference(16f)
        loginToFirebase()
    }

    private fun loginToFirebase() {
        val email = getString(R.string.firebase_email)
        val password = getString(R.string.firebase_password)

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    subscribeToUpdates()
                    Log.d(TAG, "firebase auth success");
                } else {
                    Log.d(TAG, "firebase auth failed");
                }
            }
    }

    private fun subscribeToUpdates() {
        val ref: DatabaseReference =
            FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_path))
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                setMarker(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                setMarker(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

        })
    }

    @Suppress("UNCHECKED_CAST")
    private fun setMarker(dataSnapshot: DataSnapshot) {
        val key = dataSnapshot.key ?: return
        val value: HashMap<String, Any> = dataSnapshot.value as HashMap<String, Any>

        val latlng = LatLng(
            value["latitude"].toString().toDouble(),
            value["longitude"].toString().toDouble()
        )
        if (!markers.containsKey(key)) {
            markers[key] = mMap.addMarker(
                MarkerOptions()
                    .title(key)
                    .position(latlng)
            )
        } else {
            markers[key]?.position = latlng
        }

        val builder = LatLngBounds.Builder()
        markers.values.forEach { builder.include(it.position) }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300))

    }
}