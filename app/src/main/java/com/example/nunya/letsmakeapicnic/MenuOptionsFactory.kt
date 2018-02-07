package com.example.nunya.letsmakeapicnic

import com.google.android.gms.location.places.Place
import kotlinx.android.synthetic.main.fragment_main_menu.*

/**
 * Created by Stewart Collins on 3/02/18.
 */
class MenuOptionsFactory {
    companion object {
        public fun createDefaultMenuOptions(choosePark: Boolean, wantsFood: Boolean, wantsDrinks: Boolean): MenuOptions{
            val showRoute = false
            val noStartPoint = false
            return MenuOptions(wantsFood,wantsDrinks,choosePark,noStartPoint,null,showRoute,null,null)
        }

        public fun createMenuOptions(wantsFood: Boolean, wantsDrinks: Boolean, choosePark: Boolean, noStartPoint: Boolean, chosenDay: Int?, showRoute: Boolean,
                                      startingLocation: Place?, destination: Place?): MenuOptions{
            var startingLocationParcel: PlaceParcel? = null
            var destinationParcel: PlaceParcel? = null

            if(startingLocation != null) {
                startingLocationParcel = PlaceParcel(startingLocation.latLng.latitude, startingLocation.latLng.longitude, startingLocation.name.toString(), null, PlaceParcel.CUSTOM_LOCATION)
            }

            if(destination != null) {
                destinationParcel = PlaceParcel(destination.latLng.latitude, destination.latLng.longitude, destination.name.toString(), null, PlaceParcel.CUSTOM_LOCATION)
            }

            return MenuOptions(wantsFood,wantsDrinks,choosePark,noStartPoint,chosenDay,showRoute,startingLocationParcel,destinationParcel)
        }
    }
}