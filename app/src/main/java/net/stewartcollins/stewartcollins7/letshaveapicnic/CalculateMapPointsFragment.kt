package net.stewartcollins.stewartcollins7.letshaveapicnic

import android.support.v4.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_calculate_map_points.*
import java.util.*
import kotlin.collections.ArrayList
import android.net.ConnectivityManager

/**
 * A loading fragment that retrieves all the required map data requested for the picnic
 * such as the park or destination, the current location or start point, the shops and opening hours
 * and the route to the destination. All this is done while displaying a progress bar
 *
 * Created by Stewart Collins on 1/02/18.
 */
class CalculateMapPointsFragment : Fragment() {

    private lateinit var listener: OnMapPointsCalculated
    private val fusedLocationClient: FusedLocationProviderClient by
      lazy{ LocationServices.getFusedLocationProviderClient(activity)}
    private lateinit var menuOptions: MenuOptions
    private lateinit var bundle: Bundle
    private var shopsReceived = 0
    private var shopsRequired = 0
    private var finalRequestsRequired = 0
    private var finalRequestsReceived = 0
    private lateinit var currentLocation: LatLng
    private lateinit var destinationLatLng: LatLng
    private val shopsArray: ArrayList<PlaceDataResult> = ArrayList()
    private val disposables: ArrayList<Disposable> = ArrayList()

    companion object {
        fun newInstance(menuOptions: MenuOptions): CalculateMapPointsFragment {
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

    override fun onStart() {
        super.onStart()

        if(!hasInternetConnection()){
            calculatingText.text = getString(R.string.calculating_no_internet)
            progressBar.visibility = View.GONE
            return
        }
        menuOptions = arguments.getParcelable(MenuOptions.EXTRAS_STRING)
        bundle = Bundle()
        if(menuOptions.wantsDrinks)  shopsRequired++
        if(menuOptions.wantsFood) shopsRequired++

        if(menuOptions.startingLocation == null){
            if(menuOptions.noStartPoint){
                bundle.putBoolean(getString(R.string.EXTRA_NO_START_POINT),true)
                getDestination()

            }else{
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
            }

        }else{
            currentLocation = LatLng(menuOptions.startingLocation!!.latitude,menuOptions.startingLocation!!.longitude)
            bundle.putParcelable(getString(R.string.EXTRA_STARTING_LOCATION),menuOptions.startingLocation)
            getDestination()
        }
    }

    override fun onStop() {
        super.onStop()
        for(disposable in disposables){
            if(!disposable.isDisposed){
                disposable.dispose()
            }
        }
    }

    /**
     * Copied this straight from android developer docs https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
     */
    private fun hasInternetConnection(): Boolean{
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
        return isConnected

    }

    private fun getDestination(){
        if(menuOptions.destination != null){
            val destinationParcel: PlaceParcel = menuOptions.destination as PlaceParcel
            destinationLatLng = LatLng(destinationParcel.latitude,destinationParcel.longitude)
            if(menuOptions.noStartPoint){
                currentLocation = destinationLatLng
            }
            bundle.putParcelable(getString(R.string.EXTRA_PARK_DETAILS),destinationParcel)
            if(!menuOptions.wantsFood && !menuOptions.wantsDrinks){
                if(menuOptions.showRoute){
                    getDirections(null,null)
                }else{
                    listener.onMapPointsCalculated(bundle)
                }
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
        val observerDisposable = googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            if(result.results.isNotEmpty()){
                                if(menuOptions.choosePark) {
                                    val parksArray:Array<PlaceParcel?> = arrayOfNulls<PlaceParcel>(result.results.size)
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
                                        if(menuOptions.showRoute){
                                            getDirections(null,null)
                                        }else{
                                            listener.onMapPointsCalculated(bundle)
                                        }

                                    }else{
                                        calculatingText.text = getString(R.string.calculating_shops)
                                    }

                                    if(menuOptions.wantsFood){
                                        getShops("supermarket")
                                    }
                                    if(menuOptions.wantsDrinks){
                                        getShops("liquor_store")
                                    }

                                }
                            }else{
                                Log.e("Place Location Error","Could not retrieve parks")
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
        disposables.add(observerDisposable)
    }

    private fun convertPlaceDataToParcel(placeData: PlaceData): PlaceParcel {
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
        val placeParcel = PlaceParcel(placeData.geometry.location.lat, placeData.geometry.location.lng,
                placeData.name, null, placeType)
        return placeParcel
    }

    private fun getShops(placeType: String){
        val googlePlacesApi = GooglePlacesAPIService.create()
        val location = "${destinationLatLng.latitude},${destinationLatLng.longitude}"

        val observerDisposable = if(menuOptions.dayOfWeek == null) {
                                    googlePlacesApi.searchNearbyOpenPlaces(location, getString(R.string.google_maps_key),placeType)
                                }else {
                                    googlePlacesApi.searchNearbyPlaces(location, getString(R.string.google_maps_key),placeType)
                                }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            { result ->
                                if(result.results.isNotEmpty()){
                                    shopsReceived(result)
                                }else{
                                    Log.e("Place Location Error","Could not retrieve closest $placeType")
                                }
                            }
                            ,{ error -> error.printStackTrace()}
                    )
        disposables.add(observerDisposable)
    }

    private fun getDirections(waypoint1: PlaceParcel?, waypoint2: PlaceParcel?){
        val googleDirectionsApi = GoogleDirectionsAPIService.create()
        val start = "${currentLocation.latitude},${currentLocation.longitude}"
        val destination = "${destinationLatLng.latitude},${destinationLatLng.longitude}"
        var waypoints: String? = null
        calculatingText.text = getString(R.string.calculating_route)
        if(waypoint1 != null){
            waypoints = "optimize:true|${waypoint1.latitude},${waypoint1.longitude}"
            if(waypoint2 != null){
                waypoints += "|${waypoint2.latitude},${waypoint2.longitude}"
            }
        }
        val observerDisposable = if(waypoints == null){
            googleDirectionsApi.getRouteBetweenTwoPoints(start,getString(R.string.google_maps_key),destination)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            { result ->
                                if(result != null){
                                    routeReceived(result)
                                }
                            }
                    )
        }else{
            googleDirectionsApi.getRouteWithWaypointsPoints(start,getString(R.string.google_maps_key),destination,waypoints)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            { result ->
                                if(result != null){
                                    routeReceived(result)
                                }
                            }
                    )
        }
        disposables.add(observerDisposable)
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

    private fun routeReceived(routesData: RoutesDataResponse){
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
        val observerDisposable = googlePlacesApi.getPlaceDetails(placeID, getString(R.string.google_maps_key))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { result ->
                            var openingHoursString = "No opening hours found"
                            if(result.result.opening_hours != null){
                                if(menuOptions.dayOfWeek == null){
                                    openingHoursString = result.result.opening_hours.weekday_text[getCurrentDayOfTheWeek()]
                                }else{
                                    val dayIndex = (menuOptions.dayOfWeek as Int) - 1
                                    openingHoursString = result.result.opening_hours.weekday_text[dayIndex]
                                }
                            }
                            shopParcel.openingHours = openingHoursString
                            when (shopParcel.type){
                                PlaceParcel.SUPERMARKET -> bundle.putParcelable(getString(R.string.EXTRA_SUPERMARKET_DETAILS),shopParcel)
                                PlaceParcel.SUPERMARKET_AND_LIQUOR -> bundle.putParcelable(getString(R.string.EXTRA_SUPERMARKET_DETAILS),shopParcel)
                                PlaceParcel.LIQUOR_STORE -> bundle.putParcelable(getString(R.string.EXTRA_LIQUOR_STORE_DETAILS),shopParcel)
                            }
                            if(isFinished()){
                                listener.onMapPointsCalculated(bundle)
                            }
                        }
                        ,{ error -> error.printStackTrace()}
                )
        disposables.add(observerDisposable)
    }

    private fun shopsReceived(shops: PlaceDataResult){
        shopsArray.add(shops)
        shopsReceived ++
        if (shopsReceived >= shopsRequired){
            finalRequestsRequired = shopsRequired
            if(menuOptions.showRoute){
                finalRequestsRequired++
            }
            var shop1Parcel: PlaceParcel? = null
            var shop2Parcel: PlaceParcel? = null
            if(shopsRequired == 2){
                val shopsType1 = shopsArray.get(0)
                val shopsType2 = shopsArray.get(1)
                val (shop1Index,shop2Index) = DistanceCalculator.getBestTwoShopIndexes(currentLocation, destinationLatLng, shopsType1, shopsType2)
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
                val shopIndex = DistanceCalculator.getBestShopIndex(shops, currentLocation, destinationLatLng)
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
        finalRequestsReceived ++
        if (finalRequestsReceived >= finalRequestsRequired){
            Log.v("isFinished", "true")
            return true
        }else {
            return false
        }
    }

    interface OnMapPointsCalculated{
        fun onMapPointsCalculated(bundle: Bundle)
    }
}