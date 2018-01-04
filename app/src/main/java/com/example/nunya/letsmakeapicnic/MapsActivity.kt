package com.example.nunya.letsmakeapicnic

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.location.LocationListener
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, LocationListener {


  override fun onConnectionSuspended(p0: Int) {
    Log.v("Not Implemented","onConnectionSuspended method")
  }

  override fun onConnectionFailed(p0: ConnectionResult) {
    Log.v("Not Implemented","onConnectionFailed method")
  }

  override fun onMarkerClick(p0: Marker?): Boolean {
    p0?.let {
      if(isMarkerInfoWindowShown){
        isMarkerInfoWindowShown = false
      }else{
        p0.showInfoWindow()
        isMarkerInfoWindowShown = true
      }
      return true
    }
    return false
  }

  override fun onLocationChanged(p0: Location?) {
    if(p0 != null){
      lastLocation = p0
      placeMarkerOnMap(LatLng(p0.latitude,p0.longitude))
    }
  }

  override fun onConnected(p0: Bundle?) {
    setUpMap()
    startLocationUpdates()
  }

  private lateinit var mMap: GoogleMap
  private var googleApiClient: GoogleApiClient? = null
  private var isMarkerInfoWindowShown = false
  private val LOCATION_PERMISSION_REQUEST_CODE = 1
  private var locationRequest: LocationRequest? = null
  private var locationUpdateState = false
  private val REQUEST_CHECK_SETTINGS = 2
  private val PLACE_PICKER_REQUEST = 3
  private var lastLocation: Location? = null

  private fun loadPlacePicker(){
    val placePickerIntent = PlacePicker.IntentBuilder().build(this)
    try{
      startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST)
    }catch (e: GooglePlayServicesNotAvailableException){
      e.printStackTrace()
    }catch (e: GooglePlayServicesRepairableException){
      e.printStackTrace()
    }
  }

  fun startLocationUpdates(){
    if(ActivityCompat.checkSelfPermission(this,
        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
          LOCATION_PERMISSION_REQUEST_CODE)
      return
    }
    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest, this)
  }

  fun createLocationRequest(){
    locationRequest = LocationRequest()
    locationRequest?.setInterval(1000)
    locationRequest?.setFastestInterval(5000)
    locationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

    val locationSettingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest!!).build()

    var result = LocationServices.SettingsApi
        .checkLocationSettings(googleApiClient,locationSettingsRequest)

    result.setResultCallback { result ->
      val status = result.status
      when(status.statusCode){
        LocationSettingsStatusCodes.SUCCESS -> {
          locationUpdateState = true
          startLocationUpdates()
        }
        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
          try{
            status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
          }catch(e: IntentSender.SendIntentException){
            Log.v("LocationSettings","Could not resolve result")
          }
        }LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
          Log.v("LocationSettings","Settings Change Unavailable")
        }

      }
    }
  }

  private fun placeMarkerOnMap(location: LatLng){
    val markerOptions = MarkerOptions().position(location)
    //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource
      //(getResources(), R.mipmap.ic_user_location)))
    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
    var titleTexts = getAddress(location)
    markerOptions.title(titleTexts)
    mMap.addMarker(markerOptions)
  }

  private fun getAddress(latLng: LatLng): String{
    Log.v("Address:","Finding address")
    var geocoder = Geocoder(this)
    var addressText = ""
    var addresses: List<Address>? = null
    var address: Address? = null
    try{
      addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
      if(addresses != null && !addresses.isEmpty()){
        address = addresses.get(0)
        var i = 0
        Log.v("Address:","Lines" + address.maxAddressLineIndex)
        while(i <= address.maxAddressLineIndex){
          if(i != 0){ addressText += "\n" }
          addressText += address.getAddressLine(i)
          i++
        }
      }
    }catch (e: IOException){
      Log.v("Exception","IO Exception in get Address")
    }
    Log.v("Address:",addressText)
    return addressText
  }

  private fun setUpMap() {
    if(ActivityCompat.checkSelfPermission(this,
        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
      ActivityCompat.requestPermissions(this,
          arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
    }

    mMap.setMyLocationEnabled(true)

    val locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient)
    locationAvailability?.let {
      if(locationAvailability.isLocationAvailable){
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)

        lastLocation?.let {
          val currentLocation = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
          placeMarkerOnMap(currentLocation)
          mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,12f))
        }

      }
    }
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if(googleApiClient == null){
      googleApiClient = GoogleApiClient.Builder(this)
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener { this }
          .addApi(LocationServices.API)
          .build()
    }

    setContentView(R.layout.activity_maps)
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    val mapFragment = supportFragmentManager
        .findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)
    createLocationRequest()
    val fab = findViewById<FloatingActionButton>(R.id.fab)
    fab.setOnClickListener{loadPlacePicker()}
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if(requestCode == REQUEST_CHECK_SETTINGS){
      if(resultCode == RESULT_OK){
        locationUpdateState = true
        startLocationUpdates()
      }
    }
    if(requestCode == PLACE_PICKER_REQUEST){
      if(resultCode == RESULT_OK){
        val place = PlacePicker.getPlace(this, data)
        var addressText = place.name.toString()
        addressText += "\n" + place.address.toString()

        placeMarkerOnMap(place.latLng)
      }
    }
  }

  override fun onPause() {
    super.onPause()
    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
  }

  override fun onResume() {
    super.onResume()
    if(googleApiClient!!.isConnected && !locationUpdateState){
      startLocationUpdates()
    }
  }

  override fun onStart() {
    super.onStart()
    googleApiClient?.connect()
  }

  override fun onStop() {
    super.onStop()
    if(googleApiClient != null){
      if(googleApiClient!!.isConnected){
        googleApiClient!!.disconnect()
      }
    }
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

    // Add a marker in Melbourne and move the camera
    val myPlace = LatLng(-37.8136, 144.9631)
    mMap.addMarker(MarkerOptions().position(myPlace).title("My favorite city"))
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 12f))


    mMap.getUiSettings().setZoomControlsEnabled(true)
    mMap.setOnMarkerClickListener(this)

  }
}
