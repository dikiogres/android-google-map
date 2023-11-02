package com.example.androidgooglemaps;

import android.Manifest
import android.app.Activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androidgooglemaps.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import android.location.Address
import android.location.Geocoder
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var map: GoogleMap
    private lateinit var latitudeInput: EditText
    private lateinit var longitudeInput: EditText
    private lateinit var locationNameInput: EditText // New EditText
    private lateinit var submitButton: Button
    private lateinit var submitLocationButton: Button // New Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        latitudeInput = findViewById(R.id.latitudeInput)
        longitudeInput = findViewById(R.id.longitudeInput)
        locationNameInput = findViewById(R.id.locationNameInput) // Initialize
        submitButton = findViewById(R.id.submitButton)
        submitLocationButton = findViewById(R.id.submitLocationButton) // Initialize

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap
            enableMyLocation()
        }

        submitButton.setOnClickListener {
            val latitude = latitudeInput.text.toString().toDoubleOrNull()
            val longitude = longitudeInput.text.toString().toDoubleOrNull()

            if (latitude != null && longitude != null) {
                val location = LatLng(latitude, longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            } else {
                Toast.makeText(this, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show()
            }
        }

        submitLocationButton.setOnClickListener {
            val locationName = locationNameInput.text.toString().trim()
            try {
                val location = getLocationFromName(locationName)
                location?.let {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                } ?: Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this, "Geocoder service not available", Toast.LENGTH_SHORT).show()
            }
        }


        checkLocationPermission()
    }

    private fun getLocationFromName(locationName: String): LatLng? {
        val geocoder = Geocoder(this)
        val addresses: List<Address>? = geocoder.getFromLocationName(locationName, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            return LatLng(address.latitude, address.longitude)
        }
        return null
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            checkLocationPermission()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}
