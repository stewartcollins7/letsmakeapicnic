package net.stewartcollins.stewartcollins7.letshaveapicnic

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_main_menu.*


/**
 * A fragment that displays the main menu for the app
 *
 * Created by Stewart Collins on 3/02/18.
 */
class MainMenuFragment: Fragment() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var listener: OnPicnicPlannerPressed
    private lateinit var aboutListener: OnAboutPressed

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context is OnPicnicPlannerPressed){
            listener = context
        }else{
            throw ClassCastException(context.toString() + "must implement OnPicnicPlannerPressed")
        }
        if(context is OnAboutPressed){
            aboutListener = context
        }else{
            throw ClassCastException(context.toString() + "must implement OnAboutPressed")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?  {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater!!.inflate(R.layout.fragment_main_menu, container,
                false)
        return view
    }

    private fun requestLocationPermission(){
        if(ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.ACCESS_FINE_LOCATION)){
                //Has previously denied permission show explanation
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }else{
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }

        }
    }

    override fun onStart() {
        super.onStart()

        picnicNowButton.setOnClickListener({
            if(ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context,"This app requires access to your location to function", Toast.LENGTH_LONG).show()
                requestLocationPermission()
            }else{
                val intent = Intent(context, MapsActivity::class.java)
                val menuOptions = MenuOptionsFactory.createDefaultMenuOptions(false,
                        foodCheckBox.isChecked, drinksCheckBox.isChecked)
                intent.putExtra(MenuOptions.EXTRAS_STRING,menuOptions)
                startActivity(intent)
            }
        })

        chooseParkButton.setOnClickListener({
            val intent = Intent(context, MapsActivity::class.java)
            val menuOptions = MenuOptionsFactory.createDefaultMenuOptions(true,
                    foodCheckBox.isChecked, drinksCheckBox.isChecked)
            intent.putExtra(MenuOptions.EXTRAS_STRING,menuOptions)
            startActivity(intent)
        })

        picnicPlannerButton.setOnClickListener({listener.onPicnicPlannerPressed()})
        aboutButton.setOnClickListener({aboutListener.onAboutPressed()})

        requestLocationPermission()
    }

    interface OnPicnicPlannerPressed{
        fun onPicnicPlannerPressed()
    }

    interface OnAboutPressed{
        fun onAboutPressed()
    }
}