package com.example.nunya.letsmakeapicnic

import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Stewart Collins on 3/02/18.
 */
interface GoogleDirectionsAPIService {
    @GET("json?mode=walking?")
    fun getRouteBetweenTwoPoints(@Query("origin") startLatLng: String, @Query("key") mapsKey: String, @Query("destination") destinationLatLng: String): Observable<RoutesDataResponse>

    @GET("json?")
    fun getRouteWithWaypointsPoints(@Query("origin") startLatLng: String, @Query("key") mapsKey: String, @Query("destination") destinationLatLng: String, @Query("waypoints") waypoints: String): Observable<RoutesDataResponse>


    /**
     * Companion object to create the GithubApiService
     */
    companion object Factory {
        fun create(): GoogleDirectionsAPIService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                    .build()

            return retrofit.create(GoogleDirectionsAPIService::class.java);
        }
    }
}