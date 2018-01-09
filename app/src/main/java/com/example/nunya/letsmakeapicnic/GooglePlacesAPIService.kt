package com.example.nunya.letsmakeapicnic

import android.provider.Settings.System.getString
import io.reactivex.Observable
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query

/**
 * Created by Stewart Collins on 9/01/18.
 */
interface GooglePlacesAPIService {
    @GET("json?rankby=distance")
    fun searchNearbyPlaces(@Query("location") currentLocation: String, @Query("key") mapsKey: String,
                    @Query("type") placeType: String): Observable<PlaceDataResult>

    @GET("json?rankby=distance&opennow=true")
    fun searchNearbyOpenPlaces(@Query("location") currentLocation: String, @Query("key") mapsKey: String,
                           @Query("type") placeType: String): Observable<PlaceDataResult>
    /**
     * Companion object to create the GithubApiService
     */
    companion object Factory {
        fun create(): GooglePlacesAPIService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/")
                    .build()

            return retrofit.create(GooglePlacesAPIService::class.java);
        }
    }
}