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
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MenuActivity : AppCompatActivity(), MainMenuFragment.OnPicnicPlannerPressed {
  override fun onPicnicPlannerPressed() {
    supportFragmentManager
            .beginTransaction()
            .replace(R.id.menuScreen, PicnicPlannerFragment())
            .addToBackStack(null)
            .commit()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_menu)
      supportFragmentManager
              .beginTransaction()
              .add(R.id.menuScreen, MainMenuFragment())
              .commit()

  }
}
