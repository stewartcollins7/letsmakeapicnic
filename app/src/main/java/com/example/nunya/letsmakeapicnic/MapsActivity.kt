package com.example.nunya.letsmakeapicnic

import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.location.LocationListener
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), CalculateMapPointsFragment.OnMapPointsCalculated {
  override fun onMapPointsCalculated(bundle: Bundle) {
    val mapFragment = PicnicMapFragment.newInstance(bundle)
    supportFragmentManager
            .beginTransaction()
            .replace(R.id.maps_activity, mapFragment)
            .commit()
    mapFragment.getMapAsync(mapFragment)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_maps)
    val menuOptions = intent.getParcelableExtra<MenuOptions>(MenuOptions.EXTRAS_STRING)
    val calculatePointsFragment = CalculateMapPointsFragment.newInstance(menuOptions)

    supportFragmentManager
            .beginTransaction()
            .replace(R.id.maps_activity, calculatePointsFragment)
            .commit()
  }

}
