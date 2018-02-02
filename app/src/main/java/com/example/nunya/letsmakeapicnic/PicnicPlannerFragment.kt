package com.example.nunya.letsmakeapicnic

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_picnic_planner.*

/**
 * Created by Stewart Collins on 3/02/18.
 */
class PicnicPlannerFragment : Fragment() {

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
    }
}