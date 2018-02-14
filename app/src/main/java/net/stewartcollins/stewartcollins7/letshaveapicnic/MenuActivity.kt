package net.stewartcollins.stewartcollins7.letshaveapicnic

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

/**
 * An activity that serves as the base activity for the mainMenuFragment, the aboutFragment and the picnicPlannerFragment
 *
 * Created by Stewart Collins on 3/02/18.
 */
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
    val fragmentManager = supportFragmentManager
            .beginTransaction()
            .replace(R.id.menuScreen, AboutFragment())
    if(currentMenuWindow != ABOUT_ID){
      fragmentManager.addToBackStack(null)
      currentMenuWindow = ABOUT_ID
    }

    fragmentManager.commit()
  }

  override fun onPicnicPlannerPressed() {
    val fragmentManager = supportFragmentManager
            .beginTransaction()
            .replace(R.id.menuScreen, PicnicPlannerFragment())
    if(currentMenuWindow != PICNIC_PLANNER_ID){
      fragmentManager.addToBackStack(null)
      currentMenuWindow = PICNIC_PLANNER_ID
    }
    fragmentManager.commit()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    setContentView(R.layout.activity_menu)
    super.onCreate(savedInstanceState)

    if(savedInstanceState != null){
      if(savedInstanceState.containsKey(CURRENT_MENU_WINDOW_KEY)){
        val desiredMenu = savedInstanceState.getInt(CURRENT_MENU_WINDOW_KEY)
        currentMenuWindow = desiredMenu
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
