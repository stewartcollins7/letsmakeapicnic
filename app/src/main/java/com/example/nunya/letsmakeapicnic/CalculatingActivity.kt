package com.example.nunya.letsmakeapicnic

import android.content.Intent
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList


class CalculatingActivity : AppCompatActivity() {
    private lateinit var bundle: Bundle


    private val fusedLocationClient: FusedLocationProviderClient by
         lazy{LocationServices.getFusedLocationProviderClient(this)}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculating)
    }

    override fun onStart() {
        super.onStart()
        bundle = Bundle()
        try{
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location ->
                   if(location != null){
                       bundle.putParcelable(getString(R.string.EXTRA_CURRENT_LOCATION),location)
                       if(intent.getBooleanExtra(getString(R.string.EXTRA_PARK_SPECIFIED),false)){
                           var parkParcel = intent.getParcelableExtra<PlaceParcel>(getString(R.string.EXTRA_PARK_DETAILS))
                           val locationArray = DoubleArray(4)
                           locationArray.set(0,location.latitude)
                           locationArray.set(1,location.longitude)
                           locationArray.set(2,parkParcel.latitude)
                           locationArray.set(3,parkParcel.longitude)
//                           val parkParcel = convertPlaceDataToParcel(park)
                           bundle.putParcelable(getString(R.string.EXTRA_PARK_DETAILS),parkParcel)
                           val nameArray = arrayOf(parkParcel.name)
                           val wantsDrinks = intent.getBooleanExtra(getString(R.string.EXTRA_WANTS_FOOD),false)
                           val wantsFood = intent.getBooleanExtra(getString(R.string.EXTRA_WANTS_DRINKS),false)
                           if(wantsFood){
                               getClosestSupermarket(locationArray,nameArray,wantsDrinks)
                           }else if(wantsDrinks){
                               getClosestLiquorStore(locationArray, nameArray, wantsFood, null)
                           }
                       }else{
                           getClosestPark(location)
                       }

                       //getOpeningHours("whatever")
                   }else{
                       Log.v("Location Exception", "Current location not found in CalculatingActivity, GPS may be disabled")
                   }
            }
        }catch (e: SecurityException){
            Log.e("Permission Exception", "Location permissions not available in CalculatingActivity")
        }
    }

    private fun getClosestPark(currentLocation: Location){
//        val NUMBER_OF_PARKS = 10
        val receivedBundle = intent.extras
        if(receivedBundle != null && receivedBundle.containsKey(getString(R.string.EXTRA_CUSTOM_LOCATION))){
            val customLocationParcel = receivedBundle.getParcelable<PlaceParcel>(getString(R.string.EXTRA_CUSTOM_LOCATION))
            val locationArray = DoubleArray(4)
            locationArray.set(0,currentLocation.latitude)
            locationArray.set(1,currentLocation.longitude)
            locationArray.set(2,customLocationParcel.latitude)
            locationArray.set(3,customLocationParcel.longitude)
            bundle.putParcelable(getString(R.string.EXTRA_PARK_DETAILS),customLocationParcel)
            val nameArray = arrayOf(customLocationParcel.name)
            val wantsDrinks = receivedBundle.getBoolean(getString(R.string.EXTRA_WANTS_FOOD),false)
            val wantsFood = receivedBundle.getBoolean(getString(R.string.EXTRA_WANTS_DRINKS),false)
            if(wantsFood){
                getClosestSupermarket(locationArray,nameArray,wantsDrinks)
            }else if(wantsDrinks){
                getClosestLiquorStore(locationArray, nameArray, wantsFood, null)
            }else{
                loadMapActivity(locationArray,nameArray,wantsFood,wantsDrinks, null)
            }
        }else{
            val latitude = currentLocation.latitude
            val longitude = currentLocation.longitude
            val googlePlacesApi = GooglePlacesAPIService.create()
            val location = "$latitude,$longitude"
            val placeType = "park"
            var observerDisposable = googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
//                .take(NUMBER_OF_PARKS.toLong())
                    .subscribe(
                            { result ->
                                if(result.results.size > 0){
                                    val park = result.results.first()
                                    val parkLat = park.geometry.location.lat
                                    val parkLng = park.geometry.location.lng
                                    val locationArray = DoubleArray(4)
                                    locationArray.set(0,currentLocation.latitude)
                                    locationArray.set(1,currentLocation.longitude)
                                    locationArray.set(2,parkLat)
                                    locationArray.set(3,parkLng)
                                    val parkParcel = convertPlaceDataToParcel(park)
                                    bundle.putParcelable(getString(R.string.EXTRA_PARK_DETAILS),parkParcel)
                                    val nameArray = arrayOf(park.name)
                                    val wantsDrinks = intent.getBooleanExtra(getString(R.string.EXTRA_WANTS_FOOD),false)
                                    val wantsFood = intent.getBooleanExtra(getString(R.string.EXTRA_WANTS_DRINKS),false)
                                    val choosePark = receivedBundle.getBoolean(getString(R.string.EXTRA_CHOOSE_PARK),false)
                                    if(wantsFood){
                                        getClosestSupermarket(locationArray,nameArray,wantsDrinks)
                                    }else if(wantsDrinks){
                                        getClosestLiquorStore(locationArray, nameArray, wantsFood, null)
                                    }else if(choosePark){
                                        var parksArray:Array<PlaceParcel?> = arrayOfNulls<PlaceParcel>(20)
                                        for(park in result.results.indices){
                                            val parkParcel = convertPlaceDataToParcel(result.results[park])
                                            parksArray.set(park,parkParcel)
//                                    val parkNorthEast = LatLng(park.geometry.viewport.northeast.lat,park.geometry.viewport.northeast.lng)
//                                    val parkSouthWest = LatLng(park.geometry.viewport.southwest.lat,park.geometry.viewport.southwest.lng)
//                                    Log.v("Park",park.name +":"+estimateArea(parkNorthEast,parkSouthWest)+"m")
                                        }
                                        bundle.putParcelableArray(getString(R.string.EXTRA_PARKS_ARRAY),parksArray)
                                        bundle.putBoolean(getString(R.string.EXTRA_SELECTING_PARKS),true)
                                        loadMapActivity(locationArray,nameArray,wantsFood,wantsDrinks, null)
                                    }else{
                                        loadMapActivity(locationArray,nameArray,wantsFood,wantsDrinks, null)
                                    }
                                }else{
                                    Log.e("Place Location Error","Could not retreive closest park")
                                }
                            }
                            ,{ error -> error.printStackTrace()}
                    )
        }

    }

    private fun convertPlaceDataToParcel(placeData: PlaceData): PlaceParcel{
        var placeType = 0
        for(type in placeData.types){
            when(type){
                "park" -> placeType = PlaceParcel.PARK
                "liquor_store" -> {
                    if(placeType == 0){
                        placeType = PlaceParcel.LIQUOR_STORE
                    }else if(placeType == PlaceParcel.SUPERMARKET){
                        placeType = PlaceParcel.SUPERMARKET_AND_LIQUOR
                    }
                }
                "supermarket" -> {
                    if(placeType == 0){
                        placeType = PlaceParcel.SUPERMARKET
                    }else if(placeType == PlaceParcel.LIQUOR_STORE){
                        placeType = PlaceParcel.SUPERMARKET_AND_LIQUOR
                    }
                }
            }
        }

        val placeParcel = PlaceParcel(placeData.geometry.location.lat,placeData.geometry.location.lng,
                placeData.name,null,placeType)
        return placeParcel
    }

    private fun getClosestSupermarket(locationArray: DoubleArray, nameArray: Array<String>, withDrinks: Boolean){
        val googlePlacesApi = GooglePlacesAPIService.create()
        val location = "${locationArray.get(2)},${locationArray.get(3)}"
        val placeType = "supermarket"
        var observerDisposable = googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result.results.size > 0){
                                val supermarket = result.results.first()
                                val newLocationArray = addLocationToArray(locationArray,supermarket.geometry.location)
                                val newNameArray = nameArray.plus(supermarket.name)
                                val supermarketParcel = convertPlaceDataToParcel(supermarket)
                                bundle.putParcelable(getString(R.string.EXTRA_SUPERMARKET_DETAILS),supermarketParcel)
                                getSupermarketOpeningHours(supermarket.place_id,withDrinks,newLocationArray,newNameArray,true)
                            }else{
                                Log.e("Place Location Error","Could not retreive closest supermarket")
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
    }

    private fun addLocationToArray(locationArray: DoubleArray, coordinates: Coordinates): DoubleArray{
        val locationLat = coordinates.lat
        val locationLng = coordinates.lng
        val originalSize = locationArray.size
        val newLocationArray = locationArray.copyOf(originalSize + 2)
        newLocationArray.set(originalSize,locationLat)
        newLocationArray.set(originalSize+1,locationLng)
        return newLocationArray
    }

    private fun getClosestLiquorStore(locationArray: DoubleArray, nameArray: Array<String>, withFood: Boolean, openingHoursArray: Array<String>?){
        val googlePlacesApi = GooglePlacesAPIService.create()
        val location = if (withFood) "${locationArray.get(4)},${locationArray.get(5)}" else "${locationArray.get(2)},${locationArray.get(3)}"
        val placeType = "liquor_store"
        var observerDisposable = googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result.results.size > 0){
                                val liquorStore = result.results.first()
                                val newLocationArray = addLocationToArray(locationArray,liquorStore.geometry.location)
                                val newNameArray = nameArray.plus(liquorStore.name)
                                val liquorStoreParcel = convertPlaceDataToParcel(liquorStore)
                                bundle.putParcelable(getString(R.string.EXTRA_LIQUOR_STORE_DETAILS),liquorStoreParcel)
                                getLiquorStoreOpeningHours(liquorStore.place_id,true,newLocationArray,newNameArray,withFood, openingHoursArray)
                            }else{
                                Log.e("Place Location Error","Could not retreive closest liquor store")
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
    }

    private fun getCurrentDayOfTheWeek(): Int{
        val calendar = Calendar.getInstance()
        val currentCalendarFormatDay = calendar.get(Calendar.DAY_OF_WEEK)
        var currentGoogleFormatDay = -1
        when (currentCalendarFormatDay) {
            Calendar.MONDAY -> currentGoogleFormatDay = 0
            Calendar.TUESDAY -> currentGoogleFormatDay = 1
            Calendar.WEDNESDAY -> currentGoogleFormatDay = 2
            Calendar.THURSDAY -> currentGoogleFormatDay = 3
            Calendar.FRIDAY -> currentGoogleFormatDay = 4
            Calendar.SATURDAY -> currentGoogleFormatDay = 5
            Calendar.SUNDAY -> currentGoogleFormatDay = 6
        }
        return currentGoogleFormatDay
    }

    /*Based on the haversine formula to calculate distance from latitude and longitude accessed from
    https://www.movable-type.co.uk/scripts/latlong.html
    Returns distance in metres
     */
    private fun estimateDistanceBetweenPoints(point1: LatLng, point2: LatLng): Double{
        val radiusOfEarth = 6371e3

        val latitude1radians = Math.toRadians(point1.latitude)
        val latitude2radians = Math.toRadians(point2.latitude)
        val latitudeDelta = Math.toRadians(point2.latitude - point1.latitude)
        val longitudeDelta = Math.toRadians(point2.longitude - point1.longitude)

        // a is the square of half the chord length between the points
        val a = Math.sin(latitudeDelta/2) * Math.sin(latitudeDelta/2) +
                Math.cos(latitude1radians) * Math.cos(latitude2radians) * Math.sin(longitudeDelta/2) * Math.sin(longitudeDelta/2)

        // c is the angular distance in radians
        val c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a))

        val distance = radiusOfEarth * c

        return distance

    }

    private fun estimateArea(northeastPoint: LatLng, southwestPoint: LatLng): Double{
        val northSouthDistance = estimateDistanceBetweenPoints(northeastPoint, LatLng(southwestPoint.latitude,northeastPoint.longitude))
        val eastWestDistance = estimateDistanceBetweenPoints(northeastPoint, LatLng(northeastPoint.latitude,southwestPoint.longitude))
        val roughArea = northSouthDistance * eastWestDistance
        return roughArea
    }

    private fun getSupermarketOpeningHours(placeID: String, withDrinks: Boolean, locationArray: DoubleArray, nameArray: Array<String>, withFood: Boolean){
        Log.v("Place ID",placeID)
        val googlePlacesApi = GooglePlacesAPIService.create()
        var observerDisposable = googlePlacesApi.getPlaceDetails(placeID, getString(R.string.google_maps_key))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result == null){
                                Log.v("Opening","Null response result")
                            }else{
                                var openingHoursArray: Array<String>? = null
                                if(result.result.opening_hours != null){
                                    Log.v("Not null","Supermarket hours not null")
                                    openingHoursArray = arrayOf(result.result.opening_hours.weekday_text.get(getCurrentDayOfTheWeek()))
                                    val supermarketParcel = bundle.getParcelable<PlaceParcel>(getString(R.string.EXTRA_SUPERMARKET_DETAILS))
                                    supermarketParcel.addOpeningHours(result.result.opening_hours.weekday_text)
                                    bundle.putParcelable(getString(R.string.EXTRA_SUPERMARKET_DETAILS),supermarketParcel)
                                }
                                if(withDrinks){
                                    getClosestLiquorStore(locationArray,nameArray,withFood, openingHoursArray)
                                }else{
                                    loadMapActivity(locationArray,nameArray,withFood,withDrinks, openingHoursArray)
                                }
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
    }

    private fun getLiquorStoreOpeningHours(placeID: String, withDrinks: Boolean, locationArray: DoubleArray, nameArray: Array<String>, withFood: Boolean, openingHoursArray: Array<String>?){
        Log.v("Place ID",placeID)
        val googlePlacesApi = GooglePlacesAPIService.create()
        var observerDisposable = googlePlacesApi.getPlaceDetails(placeID, getString(R.string.google_maps_key))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result == null){
                                Log.v("Opening","Null response result")
                            }else{
                                var newOpeningHoursArray: Array<String>? = openingHoursArray
                                if(result.result.opening_hours != null){
                                    Log.v("Not null","Liquor store hours not null")
                                    val liquorStoreParcel = bundle.getParcelable<PlaceParcel>(getString(R.string.EXTRA_LIQUOR_STORE_DETAILS))
                                    liquorStoreParcel.addOpeningHours(result.result.opening_hours.weekday_text)
                                    bundle.putParcelable(getString(R.string.EXTRA_LIQUOR_STORE_DETAILS),liquorStoreParcel)
                                    if(openingHoursArray == null){
                                        newOpeningHoursArray = arrayOf(result.result.opening_hours.weekday_text.get(3))
                                    }else{
                                        newOpeningHoursArray = openingHoursArray.plus(result.result.opening_hours.weekday_text.get(getCurrentDayOfTheWeek()))
                                    }
                                }

                                loadMapActivity(locationArray,nameArray,withFood,true,newOpeningHoursArray)
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
    }

    private fun loadMapActivity(locationArray: DoubleArray, nameArray: Array<String>, withFood: Boolean, withDrinks: Boolean, openingHoursArray: Array<String>?){
        var mapsIntent = Intent(this,MapsActivity::class.java)
        mapsIntent.putExtra("locationArray",locationArray)
        mapsIntent.putExtra("openingHoursArray",openingHoursArray)
        mapsIntent.putExtra("nameArray", nameArray)
        mapsIntent.putExtra("withFood",withFood)
        mapsIntent.putExtra("withDrinks",withDrinks)
        mapsIntent.putExtras(bundle)
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
