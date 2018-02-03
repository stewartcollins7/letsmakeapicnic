package com.example.nunya.letsmakeapicnic

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.fragment_picnic_planner.*

/**
 * Created by Stewart Collins on 3/02/18.
 */
class PicnicPlannerFragment : Fragment() {
    private val START_POINT_PLACE_PICKER_REQUEST = 1
    private val DESTINATION_PLACE_PICKER_REQUEST = 2

    private var startPoint: Place? = null
    private var destination: Place? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?  {
        super.onCreate(savedInstanceState)
        val view: View = inflater!!.inflate(R.layout.fragment_picnic_planner, container,
                false)
        return view
    }

    override fun onStart() {
        super.onStart()
        val arrayAdapter = ArrayAdapter.createFromResource(context,R.array.picnic_day_array,android.R.layout.simple_spinner_item)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        openingHoursSpinner.adapter = arrayAdapter

        startPointCurrentLocationRadio.setOnClickListener({
            plannerStartPointText.text = "Start Point: Current Location"
            startPoint = null
        })
        startPointNoStartPointRadio.setOnClickListener({
            plannerStartPointText.text = "Start Point: No Start Point"
            startPoint = null
        })
        startPointChooseLocationRadio.setOnClickListener({loadPlacePicker(START_POINT_PLACE_PICKER_REQUEST)})
        destinationChooseLocationRadio.setOnClickListener {loadPlacePicker(DESTINATION_PLACE_PICKER_REQUEST)}
        destinationChooseParkRadio.setOnClickListener({
            plannerDestinationText.text= "Destination: Choose Park"
            destination = null
        })
        destinationClosestParkRadio.setOnClickListener({
            plannerDestinationText.text= "Destination: Closest Park"
            destination = null
        })

        plannerPlanItButton.setOnClickListener({
            val noStartPointSelected = startPointRadioGroup.checkedRadioButtonId == startPointNoStartPointRadio.id
            val chooseDestinationSelected = destinationRadioGroup.checkedRadioButtonId == destinationChooseLocationRadio.id
            if(noStartPointSelected && !chooseDestinationSelected){
                val toast = Toast.makeText(context,"If you choose no start point you must choose a destination manually",Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER_VERTICAL,0,0)
                toast.show()
            }else{
                val chooseParkSelected = destinationRadioGroup.checkedRadioButtonId == destinationChooseParkRadio.id
                val openNow = openingHoursSpinner.selectedItem.toString().equals(arrayAdapter.getItem(0))
                var dayOfTheWeek: Int? = null
                if(!openNow) {
                    dayOfTheWeek = openingHoursSpinner.selectedItemPosition
                }
                val menuOptions = MenuOptionsFactory.createMenuOptions(plannerWantsFood.isChecked,plannerWantsDrinks.isChecked,
                        chooseParkSelected,openNow,dayOfTheWeek,plannerShowRouteCheckbox.isChecked,startPoint,destination)
                val intent = Intent(context,MapsActivity::class.java)
                intent.putExtra(getString(R.string.EXTRA_MENU_OPTIONS),menuOptions)
                startActivity(intent)
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == START_POINT_PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                startPoint = PlacePicker.getPlace(context, data)
                plannerStartPointText.text = "Start Point: " + startPoint!!.name.toString()
            }
        }else if(requestCode == DESTINATION_PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                destination = PlacePicker.getPlace(context, data)
                plannerDestinationText.text = "Destination: " + destination!!.name.toString()
            }
        }
    }

    private fun loadPlacePicker(PLACE_PICKER_REQUEST: Int){
        val placePickerIntent = PlacePicker.IntentBuilder().build(activity)
        try{
            startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST)
        }catch (e: GooglePlayServicesNotAvailableException){
            e.printStackTrace()
        }catch (e: GooglePlayServicesRepairableException){
            e.printStackTrace()
        }
    }
}