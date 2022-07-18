package com.example.geolocation_post

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    companion object{
        private val REQUEST_PERMISSION_REQUEST_CODE = 200
    }

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var btn_post: Button = findViewById(R.id.btn_postLocation)

        btn_post.setOnClickListener {
            if(ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_REQUEST_CODE)
            }else{
                getLocation()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_PERMISSION_REQUEST_CODE && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //getLocation()
                log()
            } else {
                latitude = 0.0
                longitude = 0.0
                Toast.makeText(this, "Permisssion not given!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, object :LocationCallback(){
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .removeLocationUpdates(this)
                    if (p0 != null && p0.locations.size > 0){
                        var locIndex = p0.locations.size-1
                        latitude = p0.locations.get(locIndex).latitude.dec()
                        longitude = p0.locations.get(locIndex).longitude.dec()
                    }else{
                        latitude = 0.0
                        longitude = 0.0
                    }
                    Toast.makeText(this@MainActivity, "lat :"+latitude+"lon :"+ longitude,Toast.LENGTH_SHORT).show()
                }
            }, Looper.getMainLooper())

    }

    private fun log() {
        //first get the location
        getLocation()
        // value to post
        val requestLocation = "("+latitude+","+longitude+")"
        //volly request
        val queue = Volley.newRequestQueue(this)
        val url = "https://localhost/insert.php" // designeted server address

        val stringReq : StringRequest =
            object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.toString()
                    Log.d("API Response",response.toString())
                    Toast.makeText(this,"Location Submitted",Toast.LENGTH_SHORT).show()
                },
                Response.ErrorListener { error ->
                    Log.d("API Response", error.toString())
                    Toast.makeText(this,"Network Error Occured",Toast.LENGTH_SHORT).show()
                }
            ){
                override fun getBody(): ByteArray {
                    return requestLocation.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }
}