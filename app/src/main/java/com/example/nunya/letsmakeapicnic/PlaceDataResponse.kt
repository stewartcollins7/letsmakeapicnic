package com.example.nunya.letsmakeapicnic

/**
* Created by Stewart Collins on 9/01/18.
*/

data class PlaceData (
        val geometry: CoordinateHolder,
        val name: String
)

data class CoordinateHolder (
        val location: Coordinates
)

data class Coordinates (
        val lat: Double,
        val lng: Double
)

data class PlaceDataResult(
        val results: List<PlaceData>
)