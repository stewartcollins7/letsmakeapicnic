package net.stewartcollins.stewartcollins7.letshaveapicnic

import com.google.android.gms.location.places.Place

/**
 * A factory class that creates the menu options incorporating the logic for the default options as well as
 * a function for a fully specified options set
 *
 * Created by Stewart Collins on 3/02/18.
 */
class MenuOptionsFactory {
    companion object {
        fun createDefaultMenuOptions(choosePark: Boolean, wantsFood: Boolean, wantsDrinks: Boolean): MenuOptions {
            val showRoute = true
            val noStartPoint = false
            return MenuOptions(wantsFood, wantsDrinks, choosePark, noStartPoint, null, showRoute, null, null)
        }

        fun createMenuOptions(wantsFood: Boolean, wantsDrinks: Boolean, choosePark: Boolean, noStartPoint: Boolean, chosenDay: Int?, showRoute: Boolean,
                                      startingLocation: Place?, destination: Place?): MenuOptions {
            var startingLocationParcel: PlaceParcel? = null
            var destinationParcel: PlaceParcel? = null

            if(startingLocation != null) {
                startingLocationParcel = PlaceParcel(startingLocation.latLng.latitude, startingLocation.latLng.longitude, startingLocation.name.toString(), null, PlaceParcel.CUSTOM_START)
            }

            if(destination != null) {
                destinationParcel = PlaceParcel(destination.latLng.latitude, destination.latLng.longitude, destination.name.toString(), null, PlaceParcel.CUSTOM_DESTINATION)
            }

            return MenuOptions(wantsFood, wantsDrinks, choosePark, noStartPoint, chosenDay, showRoute, startingLocationParcel, destinationParcel)
        }
    }
}