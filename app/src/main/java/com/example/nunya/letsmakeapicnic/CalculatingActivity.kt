package com.example.nunya.letsmakeapicnic

import android.content.Intent
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class CalculatingActivity : AppCompatActivity() {

    private val fusedLocationClient: FusedLocationProviderClient by
         lazy{LocationServices.getFusedLocationProviderClient(this)}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculating)
    }

    override fun onStart() {
        super.onStart()
        try{
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location ->
                   if(location != null){
                       getClosestPark(location)
                   }else{
                       Log.v("Location Exception", "Location not found in CalculatingActivity, GPS may be disabled")
                   }
            }
        }catch (e: SecurityException){
            Log.e("Permission Exception", "Location permissions not available in CalculatingActivity")
        }
    }

    private fun getClosestPark(currentLocation: Location){
        val latitude = currentLocation.latitude
        val longitude = currentLocation.longitude
        val googlePlacesApi = GooglePlacesAPIService.create()
        val location = "$latitude,$longitude"
        val placeType = "park"
        var observerDisposable = googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result.results.size > 0){
                                val park = result.results.first()
                                val parkLat = park.geometry.location.lat
                                val parkLng = park.geometry.location.lng
                                val closestPark = LatLng(parkLat,parkLng)
                                val locationArray = DoubleArray(4)
                                locationArray.set(0,currentLocation.latitude)
                                locationArray.set(1,currentLocation.longitude)
                                locationArray.set(2,parkLat)
                                locationArray.set(3,parkLng)
                                if(intent.getBooleanExtra("wantsFood",false)){
                                    if(intent.getBooleanExtra("wantsDrinks",false)){
                                        getClosestSupermarket(locationArray,park.name,true)
                                    }else{
                                        getClosestSupermarket(locationArray,park.name,false)
                                    }
                                }else if(intent.getBooleanExtra("wantsDrinks",false)){
                                    getClosestLiquorStore(locationArray, park.name, false)
                                }else{
                                    loadMapActivity(locationArray,park.name,false,false)
                                }
                            }else{
                                Log.e("Park Retrival Error","Could not retreive closest park")
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
    }

    private fun getClosestSupermarket(locationArray: DoubleArray, parkName: String, withDrinks: Boolean){
        val googlePlacesApi = GooglePlacesAPIService.create()
        val location = "${locationArray.get(2)},${locationArray.get(3)}"
        val placeType = "supermarket"
        var observerDisposable = googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result.results.size > 0){
                                val supermarketLat = result.results.first().geometry.location.lat
                                val supermarketLng = result.results.first().geometry.location.lng
                                val closestSupermarket = LatLng(supermarketLat,supermarketLng)
                                val newArraySize = 6
                                var newLocationArray = locationArray.copyOf(newArraySize)
                                newLocationArray.set(newArraySize-2,supermarketLat)
                                newLocationArray.set(newArraySize-1,supermarketLng)
                                if(withDrinks){
                                    getClosestLiquorStore(newLocationArray,parkName,true)
                                }else{
                                    loadMapActivity(newLocationArray,parkName,true,false)
                                }
                            }else{
                                Log.e("Liquor Store Error","Could not retreive closest liquor store")
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
    }

    private fun getClosestLiquorStore(locationArray: DoubleArray, parkName: String, withFood: Boolean){
        val googlePlacesApi = GooglePlacesAPIService.create()
        val location = if (withFood) "${locationArray.get(4)},${locationArray.get(5)}" else "${locationArray.get(2)},${locationArray.get(3)}"
        val placeType = "liquor_store"
        var observerDisposable = googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result.results.size > 0){
                                val liquorStoreLat = result.results.first().geometry.location.lat
                                val liquorStoreLng = result.results.first().geometry.location.lng
                                val closestLiquorStore = LatLng(liquorStoreLat,liquorStoreLng)

                                Log.v("With Food",withFood.toString())
                                val newArraySize = if (withFood) 8 else 6
                                var newLocationArray = locationArray.copyOf(newArraySize)
                                newLocationArray.set(newArraySize-2,closestLiquorStore.latitude)
                                newLocationArray.set(newArraySize-1,closestLiquorStore.longitude)
                                loadMapActivity(newLocationArray,parkName,withFood,true)
                            }else{
                                Log.e("Liquor Store Error","Could not retreive closest liquor store")
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
    }

    private fun loadMapActivity(locationArray: DoubleArray, parkName: String, withFood: Boolean, withDrinks: Boolean){
        var mapsIntent = Intent(this,MapsActivity::class.java)
        mapsIntent.putExtra("locationArray",locationArray)
        mapsIntent.putExtra("parkName", parkName)
        mapsIntent.putExtra("withFood",withFood)
        mapsIntent.putExtra("withDrinks",withDrinks)
        startActivity(mapsIntent)
    }

    private fun printReceivedItems(result: PlaceDataResult){
        for(place in result.results){
            Log.v("Place Received:",place.name)
            val parkLocation = place.geometry.location.lat.toString() + " " + place.geometry.location.lng.toString()
            Log.v("Place Coordinates", parkLocation)
        }
    }
}
