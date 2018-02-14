package net.stewartcollins.stewartcollins7.letshaveapicnic

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.IOException
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import kotlin.collections.ArrayList


/**
 * The map fragment that displays the googleMap with the parks, shops, and route all displayed that
 * were calculated in the calculateMapPointsFragment
 *
 * Created by Stewart Collins on 31/01/18.
 */
class PicnicMapFragment: SupportMapFragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener, LocationListener, GoogleMap.OnInfoWindowClickListener  {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val REQUEST_CHECK_SETTINGS = 2
    private lateinit var mMap: GoogleMap

    private var selectingParks = false
    private var googleApiClient: GoogleApiClient? = null
    private var isMarkerInfoWindowShown = false
    private var locationRequest: LocationRequest? = null
    private var locationUpdateState = false
    private var lastLocation: Location? = null
    private lateinit var listener: OnParkSelected

    companion object {
        fun newInstance(bundle: Bundle): PicnicMapFragment {
            val fragment = PicnicMapFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(googleApiClient == null){
            googleApiClient = GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener { this }
                    .addApi(LocationServices.API)
                    .build()
        }
        createLocationRequest()
    }

    override fun onPause() {
        super.onPause()
        if(googleApiClient != null && googleApiClient!!.isConnected){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
        }
    }

    override fun onResume() {
        super.onResume()
        getMapAsync(this)
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if(context is OnParkSelected){
            listener = context
        }else{
            throw ClassCastException(context.toString() + " must implement OnParkSelected")
        }
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
        }
    }

    override fun onConnected(p0: Bundle?) {
        if (activity == null) return
        startLocationUpdates()
    }

    override fun onConnectionSuspended(p0: Int) {
        if(locationUpdateState){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this)
            locationUpdateState = false
        }
    }

    private fun startLocationUpdates(){
        if (activity == null) return
        if(ActivityCompat.checkSelfPermission(activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this)
    }

    private fun createLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 1000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest!!).build()

        val result = LocationServices.SettingsApi
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
                        status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                    }catch(e: IntentSender.SendIntentException){
                        Log.v("LocationSettings","Could not resolve result")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    Log.v("LocationSettings","Settings Change Unavailable")
                }
            }
        }
    }

    private fun placeMarkerOnMap(placeParcel: PlaceParcel){
        val location = LatLng(placeParcel.latitude,placeParcel.longitude)
        val markerOptions = MarkerOptions().position(location)
        when(placeParcel.type){
            PlaceParcel.PARK -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.tree))
            PlaceParcel.LIQUOR_STORE -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.liquor))
            PlaceParcel.SUPERMARKET_AND_LIQUOR -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.supermarket))
            PlaceParcel.SUPERMARKET -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.supermarket))
            PlaceParcel.CUSTOM_START -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.start))
            PlaceParcel.CUSTOM_DESTINATION -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.destination))
        }

        val titleTexts = placeParcel.name
        markerOptions.title(titleTexts)
        var snippetText = ""
        if(selectingParks){
            snippetText = "Tap this window to select park"
        }else{
            snippetText = getAddress(location)
            snippetText = snippetText.substringBefore(",",snippetText)
            if(placeParcel.openingHours != null){
                val openingHoursString = placeParcel.openingHours
                snippetText += "\n" + openingHoursString
            }
        }
        markerOptions.snippet(snippetText)
        mMap.addMarker(markerOptions)
    }

    override fun onInfoWindowClick (p0: Marker){
        if(selectingParks){
            val parkParcel = PlaceParcel(p0.position.latitude, p0.position.longitude, p0.title, null, PlaceParcel.PARK)
            listener.onParkSelected(parkParcel)
        }
    }

    private fun getAddress(latLng: LatLng): String{
        val geocoder = Geocoder(activity)
        var addressText = ""
        var addresses: List<Address>? = null
        var address: Address? = null
        try{
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if(addresses != null && !addresses.isEmpty()){
                address = addresses.get(0)
                var i = 0
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

    private fun showCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
        mMap.setMyLocationEnabled(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CHECK_SETTINGS){
            if(resultCode == AppCompatActivity.RESULT_OK){
                locationUpdateState = true
                startLocationUpdates()
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
        val MAP_PADDING = 150
        val bundle = arguments
        var mapPoints: ArrayList<LatLng> = ArrayList()

        mMap = googleMap

        if(bundle.containsKey(getString(R.string.EXTRA_STARTING_LOCATION))){
            val startingLocation = bundle.getParcelable<PlaceParcel>(getString(R.string.EXTRA_STARTING_LOCATION))
            addToLatLngArray(mapPoints,startingLocation)
            placeMarkerOnMap(startingLocation)
        }else if(bundle.containsKey(getString(R.string.EXTRA_CURRENT_LOCATION))){
            val currentLocation = bundle.getParcelable<Location>(getString(R.string.EXTRA_CURRENT_LOCATION))
            val currentLatLng = LatLng(currentLocation.latitude,currentLocation.longitude)
            mapPoints.add(currentLatLng)
        }

        selectingParks = bundle.getBoolean(getString(R.string.EXTRA_SELECTING_PARKS),false)
        if(selectingParks){
            val parksArray = bundle.getParcelableArray(getString(R.string.EXTRA_PARKS_ARRAY))
            for(park in parksArray){
                if(park is PlaceParcel){
                    placeMarkerOnMap(park)
                    addToLatLngArray(mapPoints,park)
                }
            }
        }
        else{
            val parkParcel = bundle.getParcelable<PlaceParcel>(getString(R.string.EXTRA_PARK_DETAILS))
            if(parkParcel == null){
                Log.v("Park parcel", "Park info is null")
            }else{
                placeMarkerOnMap(parkParcel)
                addToLatLngArray(mapPoints,parkParcel)
            }

            if(bundle.containsKey(getString(R.string.EXTRA_LIQUOR_STORE_DETAILS))){
                val liquorStoreParcel = bundle.getParcelable<PlaceParcel>(getString(R.string.EXTRA_LIQUOR_STORE_DETAILS))
                placeMarkerOnMap(liquorStoreParcel)
                addToLatLngArray(mapPoints,liquorStoreParcel)
            }
            if(bundle.containsKey(getString(R.string.EXTRA_SUPERMARKET_DETAILS))){
                val supermarketParcel = bundle.getParcelable<PlaceParcel>(getString(R.string.EXTRA_SUPERMARKET_DETAILS))
                placeMarkerOnMap(supermarketParcel)
                addToLatLngArray(mapPoints,supermarketParcel)
            }
        }
        if(bundle.containsKey(getString(R.string.EXTRA_ROUTE))){
            val route: Array<LatLng> = bundle.getParcelableArray(getString(R.string.EXTRA_ROUTE)) as Array<LatLng>
            mapPoints = route.toCollection(ArrayList<LatLng>())
            val lineOptions = PolylineOptions()
            val routeMutableList = route.toMutableList()
            lineOptions.addAll(routeMutableList)
            lineOptions.width(10f)
            lineOptions.color(Color.MAGENTA)
            mMap.addPolyline(lineOptions)
        }

        val latLngBounds = calculateMapBounds(mapPoints)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,MAP_PADDING))
        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.setOnMarkerClickListener(this)
        mMap.setOnInfoWindowClickListener(this)
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(context))
        Toast.makeText(context,"Tap the markers for more information",Toast.LENGTH_SHORT).show()
        showCurrentLocation()
    }

    private fun addToLatLngArray(array: ArrayList<LatLng>, place: PlaceParcel){
        val latLng = LatLng(place.latitude,place.longitude)
        array.add(latLng)
    }

    private fun calculateMapBounds(mapPoints: ArrayList<LatLng>): LatLngBounds? {
        if(mapPoints.size < 1) return null
        val latLngBoundsBuilder = LatLngBounds.builder()
        for(mapPoint in mapPoints){
            latLngBoundsBuilder.include(mapPoint)
        }
        return latLngBoundsBuilder.build()
    }

    interface OnParkSelected{
        fun onParkSelected(park: PlaceParcel)
    }
}