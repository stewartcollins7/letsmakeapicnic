package net.stewartcollins.stewartcollins7.letshaveapicnic

import com.google.android.gms.maps.model.LatLng

/**
 * A helper class that performs as-the-crow-flies distance estimations on the given sets of map points to
 * give a rough idea of which will be the most efficient shops to visit on the way to the picnic
 *
 * Created by Stewart Collins on 2/02/18.
 */
class DistanceCalculator {
    companion object {
        /**
         * Returns the index of the best shop of type1, then best shop of type2 (type1Index, type2Index)
         */
        fun getBestTwoShopIndexes(startPoint: LatLng, destination: LatLng, shopsType1Result: PlaceDataResult, shopsType2Result: PlaceDataResult): Pair<Int,Int>{
            val shopsType1 = shopsType1Result.results
            val shopsType2 = shopsType2Result.results
            val routeDistance = ArrayList<Pair<Double,Pair<Int,Int>>>()
            for(shop1Index in 0.. (shopsType1.size - 1)){
                for(shop2Index in 0.. (shopsType2.size - 1)){
                    val shop1 = shopsType1[shop1Index]
                    val shop1Location = LatLng(shop1.geometry.location.lat,shop1.geometry.location.lng)
                    val shop2 = shopsType2[shop2Index]
                    val shop2Location = LatLng(shop2.geometry.location.lat,shop2.geometry.location.lng)
                    val distance = calculateShortestRouteTwoShops(startPoint, shop1Location, shop2Location, destination)
                    routeDistance.add(Pair(distance, Pair(shop1Index,shop2Index)))
                }
            }
            routeDistance.sortBy { (distance,_) -> distance }
            val (_,shopIndexes) = routeDistance.first()
            return shopIndexes
        }

        fun getBestShopIndex(result: PlaceDataResult, currentLocation: LatLng, destination: LatLng): Int{
            val sortedShops = getSortedDistances(result, currentLocation, destination)
            val (_,bestShopIndex) = sortedShops.first()
            return bestShopIndex
        }

        private fun calculateShortestRouteTwoShops(startPoint: LatLng, shop1: LatLng, shop2: LatLng, destination: LatLng): Double{
            val distance1 = estimateDistanceViaPoints(startPoint, shop1, shop2) + estimateDistanceBetweenPoints(shop2, destination)
            val distance2 = estimateDistanceViaPoints(startPoint, shop2, shop1) + estimateDistanceBetweenPoints(shop1, destination)
            if (distance1 > distance2){
                return distance2
            }else{
                return distance1
            }
        }

        private fun getSortedDistances(result: PlaceDataResult, currentLocation: LatLng, destination: LatLng): List<Pair<Double,Int>>{
            val locations = ArrayList<LatLng>()
            for(place in result.results){
                val latLng = LatLng(place.geometry.location.lat,place.geometry.location.lng)
                locations.add(latLng)
            }
            val distances = calculateDistancesForEachPlace(currentLocation, destination, locations)
            return createSortedSequenceFromDistances(distances)
        }

        private fun createSortedSequenceFromDistances(doubleArray: DoubleArray): List<Pair<Double,Int>>{
            val indexes = Array(doubleArray.size, {i -> i})
            val indexedPairs = doubleArray.zip(indexes)
            val sortedPairs = indexedPairs.sortedBy { (distance,index) -> distance }
            return sortedPairs
        }

        private fun calculateDistancesForEachPlace(currentLocation: LatLng, destination: LatLng, places: ArrayList<LatLng>): DoubleArray{
            val distances = DoubleArray(places.size)
            for(place in places.indices){
                val distance = estimateDistanceViaPoints(currentLocation, places.get(place), destination)
                distances[place] = distance
            }
            return distances
        }

        private fun estimateDistanceViaPoints(startPoint: LatLng, midPoint: LatLng, endPoint: LatLng): Double{
            return estimateDistanceBetweenPoints(startPoint, midPoint) + estimateDistanceBetweenPoints(midPoint, endPoint)
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
    }


}