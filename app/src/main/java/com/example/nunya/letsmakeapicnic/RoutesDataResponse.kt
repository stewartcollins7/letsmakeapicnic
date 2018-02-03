package com.example.nunya.letsmakeapicnic

/**
 * Created by Stewart Collins on 3/02/18.
 */
data class RoutesDataResponse (val routes: List<Route>)

data class Leg(val steps: List<Step>)

data class Route(val legs: List<Leg>)

data class Step(val start_location: Coordinates,
                val end_location: Coordinates)