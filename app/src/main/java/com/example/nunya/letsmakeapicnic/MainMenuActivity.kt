package com.example.nunya.letsmakeapicnic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main_menu)
    mainMenuButton.setOnClickListener({
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"This app requires access to your location to function",Toast.LENGTH_LONG).show()
            requestLocationPermission()
        }else{
            val intent = Intent(this, CalculatingActivity::class.java)
            if(foodCheckBox.isChecked){
                intent.putExtra("wantsFood",true)
            }else{
                intent.putExtra("wantsFood",false)
            }
            if(drinksCheckBox.isChecked){
                intent.putExtra("wantsDrinks",true)
            }else{
                intent.putExtra("wantsDrinks",false)
            }
            startActivity(intent)
        }
    })
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
