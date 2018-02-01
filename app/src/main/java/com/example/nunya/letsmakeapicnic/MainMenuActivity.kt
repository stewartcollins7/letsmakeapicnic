package com.example.nunya.letsmakeapicnic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val PLACE_PICKER_REQUEST = 2

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main_menu)
    mainMenuButton2.setOnClickListener({
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"This app requires access to your location to function",Toast.LENGTH_LONG).show()
            requestLocationPermission()
        }else{
            val intent = Intent(this, MainMenuActivity::class.java)
            val menuOptions = createMenuOptions(false,null,null)
            intent.putExtra(MenuOptions.EXTRAS_STRING,menuOptions)
            startActivity(intent)
        }
    })

      mainMenuButton3.setOnClickListener({
          val intent = Intent(this,MainMenuActivity::class.java)
          val menuOptions = createMenuOptions(true,null,null)
          intent.putExtra(MenuOptions.EXTRAS_STRING,menuOptions)
          startActivity(intent)
      })

      mainMenuButton.setOnClickListener({loadPlacePicker()})
  }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                val place = PlacePicker.getPlace(this, data)
                val intent = Intent(this, MainMenuActivity::class.java)
                val menuOptions = createMenuOptions(false,null,place)
                intent.putExtra(MenuOptions.EXTRAS_STRING,menuOptions)
                startActivity(intent)

//                var addressText = place.name.toString()
//                addressText += "\n" + place.address.toString()
            }
        }
    }

    private fun createMenuOptions(choosePark: Boolean, startingLocation: Place?, destination: Place?): MenuOptions{
        val wantsFood = foodCheckBox.isChecked
        val wantsDrinks = drinksCheckBox.isChecked
        var startingLocationParcel: PlaceParcel? = null
        var destinationParcel: PlaceParcel? = null

        if(startingLocation != null) {
            startingLocationParcel = PlaceParcel(startingLocation.latLng.latitude, startingLocation.latLng.longitude, startingLocation.name.toString(), null, PlaceParcel.CUSTOM_LOCATION)
        }

        if(destination != null) {
            destinationParcel = PlaceParcel(destination.latLng.latitude, destination.latLng.longitude, destination.name.toString(), null, PlaceParcel.CUSTOM_LOCATION)
        }

        return MenuOptions(wantsFood,wantsDrinks,choosePark,startingLocationParcel,destinationParcel)
    }

    private fun loadPlacePicker(){
    val placePickerIntent = PlacePicker.IntentBuilder().build(this)
    try{
      startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST)
    }catch (e: GooglePlayServicesNotAvailableException){
      e.printStackTrace()
    }catch (e: GooglePlayServicesRepairableException){
      e.printStackTrace()
    }
  }

    override fun onStart() {
        super.onStart()
        requestLocationPermission()
    }

    private fun requestLocationPermission(){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                //Has previously denied permission show explanation
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }

        }
    }
}
