package net.stewartcollins.stewartcollins7.letshaveapicnic

/**
 * These classes represent the data recieved from the GooglePlacesAPIService for place data
 *
* Created by Stewart Collins on 9/01/18.
*/

data class PlaceData (
        val geometry: CoordinateHolder,
        val name: String,
        val place_id: String,
        val types: List<String>
)

data class CoordinateHolder (
        val location: Coordinates,
        val viewport: MapCornerCoordinates
)

data class MapCornerCoordinates(
        val northeast: Coordinates,
        val southwest: Coordinates
)

data class Coordinates (
        val lat: Double,
        val lng: Double
)

data class PlaceDataResult(
        val results: List<PlaceData>
)