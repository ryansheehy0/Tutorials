package com.example.android_location_tutorial

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnSuccessListener
import java.util.Locale

/* Notes
3 places to get location. All 3 can be used with the FusedLocationProvider Class.
    - GPS/Satellite
        - Most precise, but most battery power
    - Cell tower
    - Known Wifi locations
 */
private const val PERMISSIONS_FINE_LOCATION: Int = 99
class MainActivity : AppCompatActivity() {
    private val DEFAULT_UPDATE_INTERVAL: Long = 30 //seconds
    private val FAST_UPDATE_INTERVAL: Long = 5 //seconds
    // Declare UI elements
    // TextViews
    private lateinit var tv_lat: TextView
    private lateinit var tv_lon: TextView
    private lateinit var tv_altitude: TextView
    private lateinit var tv_accuracy: TextView
    private lateinit var tv_speed: TextView
    private lateinit var tv_address: TextView
    private lateinit var tv_sensor: TextView
    private lateinit var tv_updates: TextView
    // Switch
    private lateinit var sw_locationsupdates: Switch
    private lateinit var sw_gps: Switch
    // Declaring other variables
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallBack: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        // TextViews
        tv_lat = findViewById(R.id.tv_lat)
        tv_lon = findViewById(R.id.tv_lon)
        tv_altitude = findViewById(R.id.tv_altitude)
        tv_accuracy = findViewById(R.id.tv_accuracy)
        tv_speed = findViewById(R.id.tv_speed)
        tv_address = findViewById(R.id.tv_address)
        tv_sensor = findViewById(R.id.tv_sensor)
        tv_updates = findViewById(R.id.tv_updates)
        // Switch
        sw_locationsupdates = findViewById(R.id.sw_locationsupdates)
        sw_gps = findViewById(R.id.sw_gps)

        // Location request properties
        locationRequest = LocationRequest.Builder(DEFAULT_UPDATE_INTERVAL * 1000)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

        // Event is triggered whenever the update interval is met
        locationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                // Save the location
                var location: Location? = locationResult.lastLocation
                if(location != null){
                    updateUIValues(location)
                }
            }
        }

        // GPS Switch listener
        sw_gps.setOnClickListener(View.OnClickListener {
            if (sw_gps.isChecked()) {
                // Most accurate - use GPS
                locationRequest = LocationRequest.Builder(FAST_UPDATE_INTERVAL * 1000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()
                tv_sensor.setText("Using GPS sensors")
                startLocationUpdates()
            }else {
                locationRequest = LocationRequest.Builder(DEFAULT_UPDATE_INTERVAL * 1000)
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .build()
                tv_sensor.setText("Using Towers + WIFI")
                startLocationUpdates()
            }
        })

        // location updates Switch listener
        sw_locationsupdates.setOnClickListener(View.OnClickListener {
            if(sw_locationsupdates.isChecked()){
                // turn on location tracking
                startLocationUpdates()
            }else{
               // turn off location tracking
                stopLocationUpdates()
            }
        })

        updateWithLastLocation()
    } // end onCreate

    private fun updateWithLastLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if ( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
            // User provided the permission
            // Get the last location
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, OnSuccessListener{location ->
                // Put location info into UI
                updateUIValues(location)
            })
        }else{
            // permission not granted yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ // Marshmallow (6.0) or later which use runtime permission systems
                // Ask for permissions
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_FINE_LOCATION)
            }
        }
    } // end updateGPS

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_FINE_LOCATION){
           if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
               // Permissions granted
               updateWithLastLocation()
           }else{
               Toast.makeText(this, "This app requires permissions to be granted in order to work properly.", Toast.LENGTH_SHORT).show()
               finish()
           }
        }
    } // end onRequestPermissionsResult

    private fun updateUIValues(location: Location) {
        // Update all the text view objects with the new location
        var latitude = location.latitude
        var longitude = location.longitude
        tv_lat.setText(latitude.toString())
        tv_lon.setText(longitude.toString())
        tv_accuracy.setText(location.accuracy.toString())

        if (location.hasAltitude()){
            tv_altitude.setText(location.altitude.toString())
        }else{
            tv_altitude.setText("Not available")
        }
        if (location.hasSpeed()){
            tv_speed.setText(location.speed.toString())
        }else{
            tv_speed.setText("Not available")
        }
        /*
        var geocoder: Geocoder = Geocoder(this, Locale("en_us", "United States"))
        geocoder.getFromLocation(latitude, longitude, 1,object: Geocoder.GeocodeListener{
            override fun onGeocode(addresses: MutableList<Address>) {
                tv_address.setText(addresses.get(0).getAddressLine(0))
            }

            override fun onError(errorMessage: String?) {
                tv_address.setText("Not available")
            }
        })
        */
        // Can use lower os version
        var geocoder: Geocoder = Geocoder(this)
        try {
            var addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses == null){
                tv_address.setText("Not available")
                return
            }
            tv_address.setText(addresses.get(0).getAddressLine(0))
        }catch (e: Exception){
            tv_address.setText("Not available")
        }

    } // end updateUIValues

    private fun startLocationUpdates(){
        tv_updates.setText("On")
        // Double check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (sw_gps.isChecked()) {
            tv_sensor.setText("Using GPS sensors")
        }else{
            tv_sensor.setText("Using Towers + WIFI")
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null)
        updateWithLastLocation()
    } // end startLocationUpdates

    private fun stopLocationUpdates(){
        tv_updates.setText("Off")
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    } // end stopLocationUpdates
}