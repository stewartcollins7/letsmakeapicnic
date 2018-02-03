package com.example.nunya.letsmakeapicnic

import android.support.v4.app.Fragment
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_calculate_map_points.*
import kotlin.collections.ArrayList

/**
 * Created by Stewart Collins on 1/02/18.
 */
class CalculateMapPointsFragment : Fragment() {

    private lateinit var listener: OnMapPointsCalculated
    private val fusedLocationClient: FusedLocationProviderClient by
      lazy{ LocationServices.getFusedLocationProviderClient(activity)}
    private lateinit var menuOptions: MenuOptions
    private lateinit var bundle: Bundle
    private var shopsRecieved = 0
    private var shopsRequired = 0
    private var finalRequestsRequired = 0
    private var finalRequestsRecieved = 0
    private lateinit var currentLocation: LatLng
    private lateinit var destinationLatLng: LatLng
    private val shopsArray: ArrayList<PlaceDataResult> = ArrayList()

    companion object {
        fun newInstance(menuOptions: MenuOptions): CalculateMapPointsFragment{
            val args = Bundle()
            args.putParcelable(MenuOptions.EXTRAS_STRING,menuOptions)
            val fragment = CalculateMapPointsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if(context is OnMapPointsCalculated){
            listener = context
        }else{
            throw ClassCastException(context.toString() + " must implement OnMapPointsCalculated")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?  {
        super.onCreate(savedInstanceState)
        val view: View = inflater!!.inflate(R.layout.fragment_calculate_map_points, container,
                false)
        return view
    }

    interface OnMapPointsCalculated{
        fun onMapPointsCalculated(bundle: Bundle)
    }

    override fun onStart() {
        super.onStart()
        menuOptions = arguments.getParcelable(MenuOptions.EXTRAS_STRING)
        bundle = Bundle()
        if(menuOptions.wantsDrinks)  shopsRequired++
        if(menuOptions.wantsFood) shopsRequired++

        if(menuOptions.startingLocation == null){
            try{
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    location ->
                    if(location != null){
                        currentLocation = LatLng(location.latitude,location.longitude)
                        bundle.putParcelable(getString(R.string.EXTRA_CURRENT_LOCATION),location)
                        Log.v("Current Location:",currentLocation.toString())
                        getDestination()
                    }else{
                        Log.v("Location Exception", "Current location not found in CalculatingMapPointsFragment, GPS may be disabled")
                    }
                }
            }catch (e: SecurityException){
                Log.e("Permission Exception", "Location permissions not available in CalculatingMapPointsFragment")
            }
        }else{
            currentLocation = LatLng(menuOptions.startingLocation!!.latitude,menuOptions.startingLocation!!.longitude)
            bundle.putParcelable(getString(R.string.EXTRA_STARTING_LOCATION),menuOptions.startingLocation)
            getDestination()
        }


    }

    private fun getDestination(){
        if(menuOptions.destination != null){
            var destinationParcel: PlaceParcel = menuOptions.destination as PlaceParcel
            destinationLatLng = LatLng(destinationParcel.latitude,destinationParcel.longitude)
            bundle.putParcelable(getString(R.string.EXTRA_PARK_DETAILS),destinationParcel)
            if(!menuOptions.wantsFood && !menuOptions.wantsDrinks){
                listener.onMapPointsCalculated(bundle)
            }
            else{
                calculatingText.text = getString(R.string.calculating_shops)
                if(menuOptions.wantsFood){
                    getShops("supermarket")
                }
                if(menuOptions.wantsDrinks){
                    getShops("liquor_store")
                }
            }
        }else{
            calculatingText.text = getString(R.string.calculating_parks)
            getClosestPark()
        }
    }

    private fun getClosestPark(){
        val googlePlacesApi = GooglePlacesAPIService.create()
        val location = "${currentLocation.latitude},${currentLocation.longitude}"
        val placeType = "park"
        var observerDisposable = googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result.results.size > 0){
                                if(menuOptions.choosePark) {
                                    var parksArray:Array<PlaceParcel?> = arrayOfNulls<PlaceParcel>(result.results.size)
                                    for(park in result.results.indices){
                                        val parkParcel = convertPlaceDataToParcel(result.results[park])
                                        parksArray.set(park,parkParcel)
                                    }
                                    bundle.putParcelableArray(getString(R.string.EXTRA_PARKS_ARRAY),parksArray)
                                    bundle.putBoolean(getString(R.string.EXTRA_SELECTING_PARKS),true)
                                    listener.onMapPointsCalculated(bundle)
                                }else {
                                    val park = result.results.first()
                                    val parkParcel = convertPlaceDataToParcel(park)
                                    destinationLatLng = LatLng(parkParcel.latitude,parkParcel.longitude)
                                    Log.v("Destination:",destinationLatLng.toString())
                                    bundle.putParcelable(getString(R.string.EXTRA_PARK_DETAILS),parkParcel)
                                    if(!menuOptions.wantsFood && !menuOptions.wantsDrinks){
                                        listener.onMapPointsCalculated(bundle)
                                    }
                                    calculatingText.text = getString(R.string.calculating_shops)
                                    if(menuOptions.wantsFood){
//                                        getClosestShop("supermarket")
                                        getShops("supermarket")
                                    }
                                    if(menuOptions.wantsDrinks){
//                                        getClosestShop("liquor_store")
                                        getShops("liquor_store")
                                    }

                                }
                            }else{
                                Log.e("Place Location Error","Could not retreive parks")
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
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

    private fun getShops(placeType: String){
        val googlePlacesApi = GooglePlacesAPIService.create()
        val location = "${destinationLatLng.latitude},${destinationLatLng.longitude}"
        var observerDisposable = googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result.results.size > 0){
                                shopsRecieved(result)
                            }else{
                                Log.e("Place Location Error","Could not retreive closest $placeType")
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
    }

    private fun getDirections(waypoint1: PlaceParcel?, waypoint2: PlaceParcel?){
        val googleDirectionsApi = GoogleDirectionsAPIService.create()
        val start = "${currentLocation.latitude},${currentLocation.longitude}"
        val destination = "${destinationLatLng.latitude},${destinationLatLng.longitude}"
        var waypoints: String? = null
        if(waypoint1 != null){
            waypoints = "optimize:true|${waypoint1.latitude},${waypoint1.longitude}"
            if(waypoint2 != null){
                waypoints += "|${waypoint2.latitude},${waypoint2.longitude}"
            }
        }
        if(waypoints == null){
            val observerDisposable = googleDirectionsApi.getRouteBetweenTwoPoints(start,getString(R.string.google_maps_key),destination)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            { result ->
                                if(result != null){
                                    routeRecieved(result)
                                }
                            }
                    )
        }else{
            val observerDisposable = googleDirectionsApi.getRouteWithWaypointsPoints(start,getString(R.string.google_maps_key),destination,waypoints)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            { result ->
                                if(result != null){
                                    routeRecieved(result)
                                }
                            }
                    )
        }
    }

    private fun routeRecieved(routesData: RoutesDataResponse){
        val route = routesData.routes[0]
        val routeArray = convertRouteToLatLngArray(route)
        bundle.putParcelableArray(getString(R.string.EXTRA_ROUTE),routeArray)
        if(isFinished()){
            listener.onMapPointsCalculated(bundle)
        }
    }

    private fun convertRouteToLatLngArray(route: Route): Array<LatLng>{
        val arrayList = ArrayList<LatLng>()
        for(leg in route.legs){
            for(step in leg.steps){
                val point = LatLng(step.start_location.lat,step.start_location.lng)
                arrayList.add(point)
            }
        }
        val finalStep = route.legs.last().steps.last()
        val finalPoint = LatLng(finalStep.end_location.lat,finalStep.end_location.lng)
        arrayList.add(finalPoint)
        val array: Array<LatLng> = arrayList.toTypedArray()
        return array
    }

    private fun getShopOpeningHours(placeID: String, shopParcel: PlaceParcel){
        val googlePlacesApi = GooglePlacesAPIService.create()
        var observerDisposable = googlePlacesApi.getPlaceDetails(placeID, getString(R.string.google_maps_key))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            var openingHoursArray: Array<String>? = null
                            if(result.result.opening_hours != null){
                                shopParcel.addOpeningHours(result.result.opening_hours.weekday_text)
                                when (shopParcel.type){
                                    PlaceParcel.SUPERMARKET -> bundle.putParcelable(getString(R.string.EXTRA_SUPERMARKET_DETAILS),shopParcel)
                                    PlaceParcel.SUPERMARKET_AND_LIQUOR -> bundle.putParcelable(getString(R.string.EXTRA_SUPERMARKET_DETAILS),shopParcel)
                                    PlaceParcel.LIQUOR_STORE -> bundle.putParcelable(getString(R.string.EXTRA_LIQUOR_STORE_DETAILS),shopParcel)
                                }
                            }
                            if(isFinished()){
                                listener.onMapPointsCalculated(bundle)
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
    }

    private fun shopsRecieved(shops: PlaceDataResult){
        shopsArray.add(shops)
        shopsRecieved ++
        if (shopsRecieved >= shopsRequired){
            finalRequestsRequired = shopsRequired
            if(menuOptions.showRoute){
                finalRequestsRequired++
            }
            var shop1Parcel: PlaceParcel? = null
            var shop2Parcel: PlaceParcel? = null
            if(shopsRequired == 2){
                val shopsType1 = shopsArray.get(0)
                val shopsType2 = shopsArray.get(1)
                val (shop1Index,shop2Index) = DistanceCalculator.getBestTwoShopIndexes(currentLocation,destinationLatLng,shopsType1,shopsType2)
                val shop1 = shopsType1.results[shop1Index]
                val shop2 = shopsType2.results[shop2Index]
                shop1Parcel = convertPlaceDataToParcel(shop1)
                Log.v("Shop1:",shop1Parcel.latitude.toString() +","+shop1Parcel.longitude.toString())
                shop2Parcel = convertPlaceDataToParcel(shop2)
                Log.v("Shop2:",shop2Parcel.latitude.toString() +","+shop2Parcel.longitude.toString())
                getShopOpeningHours(shop1.place_id,shop1Parcel)
                getShopOpeningHours(shop2.place_id,shop2Parcel)
            }else if(shopsRequired == 1){
                val shops = shopsArray.get(0)
                val shopIndex = DistanceCalculator.getBestShopIndex(shops,currentLocation,destinationLatLng)
                val shop = shops.results[shopIndex]
                shop1Parcel = convertPlaceDataToParcel(shop)
                getShopOpeningHours(shop.place_id,shop1Parcel)
            }
            if(menuOptions.showRoute){
                getDirections(shop1Parcel,shop2Parcel)
            }
        }
    }

    private fun isFinished(): Boolean{
        finalRequestsRecieved ++
        if (finalRequestsRecieved >= finalRequestsRequired){
            Log.v("isFinished", "true")
            return true
        }else {
            return false
        }

    }

    private fun printSortedList(sortedDistances: List<Pair<Double,Int>>){
        for((distance,index) in sortedDistances){
            Log.v("Calculated Distances","Distances:$distance  Index:$index")
        }
    }

    private fun printReceivedItems(result: PlaceDataResult){
        for(place in result.results){
            Log.v("Place Received:",place.name)
            val parkLocation = place.geometry.location.lat.toString() + " " + place.geometry.location.lng.toString()
            Log.v("Place Coordinates", parkLocation)
        }
    }
}