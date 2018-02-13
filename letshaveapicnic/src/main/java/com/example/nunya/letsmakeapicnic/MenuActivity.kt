package com.example.nunya.letsmakeapicnic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MenuActivity : AppCompatActivity(), MainMenuFragment.OnPicnicPlannerPressed, MainMenuFragment.OnAboutPressed {
  private var currentMenuWindow = 0
  private val MAIN_MENU_ID = 1
  private val PICNIC_PLANNER_ID = 2
  private val ABOUT_ID = 3
  private val CURRENT_MENU_WINDOW_KEY = "currentMenuWindowKey"

  override fun onBackPressed() {
    if(currentMenuWindow != MAIN_MENU_ID){
      currentMenuWindow = MAIN_MENU_ID
    }
    super.onBackPressed()
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    outState?.putInt(CURRENT_MENU_WINDOW_KEY,currentMenuWindow)
    super.onSaveInstanceState(outState)
  }

  override fun onAboutPressed() {
    currentMenuWindow = ABOUT_ID
    supportFragmentManager
            .beginTransaction()
            .replace(R.id.menuScreen, AboutFragment())
            .addToBackStack(null)
            .commit()
  }

  override fun onPicnicPlannerPressed() {
    currentMenuWindow = PICNIC_PLANNER_ID
    supportFragmentManager
            .beginTransaction()
            .replace(R.id.menuScreen, PicnicPlannerFragment())
            .addToBackStack(null)
            .commit()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    setContentView(R.layout.activity_menu)
    super.onCreate(savedInstanceState)

    if(savedInstanceState != null){
      if(savedInstanceState.containsKey(CURRENT_MENU_WINDOW_KEY)){
        val desiredMenu = savedInstanceState.getInt(CURRENT_MENU_WINDOW_KEY)
        when(desiredMenu){
          ABOUT_ID -> {
            onAboutPressed()
            return
          }
          PICNIC_PLANNER_ID -> {
            onPicnicPlannerPressed()
            return
          }
        }
      }
    }

    currentMenuWindow = MAIN_MENU_ID
      supportFragmentManager
              .beginTransaction()
              .add(R.id.menuScreen, MainMenuFragment())
              .commit()
  }
}
