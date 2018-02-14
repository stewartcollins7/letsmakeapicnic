package net.stewartcollins.stewartcollins7.letshaveapicnic

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

/**
 * The activity that serves as the base activity for the calculateMapPointsFragment and the PicnicMapFragment
 *
 * Created by Stewart Collins on 3/02/18.
 */
class MapsActivity : AppCompatActivity(), CalculateMapPointsFragment.OnMapPointsCalculated, PicnicMapFragment.OnParkSelected {
  private lateinit var menuOptions: MenuOptions
  private var mapBundle: Bundle? = null
  private val MAP_BUNDLE_KEY = "mapBundleKey"

  override fun onParkSelected(park: PlaceParcel) {
    menuOptions.choosePark = false
    menuOptions.destination = park
    loadCalculateFragment(menuOptions)
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    if(mapBundle != null && outState != null){
      outState.putBundle(MAP_BUNDLE_KEY,mapBundle)
    }
    super.onSaveInstanceState(outState)
  }

  override fun onMapPointsCalculated(bundle: Bundle) {
    mapBundle = bundle
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
    if(savedInstanceState != null){
      if(savedInstanceState.containsKey(MAP_BUNDLE_KEY)){
        mapBundle = savedInstanceState.getBundle(MAP_BUNDLE_KEY)
        onMapPointsCalculated(mapBundle!!)
        return
      }
    }
    menuOptions = intent.getParcelableExtra<MenuOptions>(MenuOptions.EXTRAS_STRING)
    loadCalculateFragment(menuOptions)

  }

  private fun loadCalculateFragment(menuOptions: MenuOptions){
    val calculatePointsFragment = CalculateMapPointsFragment.newInstance(menuOptions)

    supportFragmentManager
            .beginTransaction()
            .replace(R.id.maps_activity, calculatePointsFragment)
            .commit()
  }

}
