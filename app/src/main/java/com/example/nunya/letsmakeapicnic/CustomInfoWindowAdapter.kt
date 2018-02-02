package com.example.nunya.letsmakeapicnic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.infowindow_custom.view.*

/**
 * Created by Stewart Collins on 1/02/18.
 */
class CustomInfoWindowAdapter(context: Context): GoogleMap.InfoWindowAdapter {
    private var layoutInflater: LayoutInflater

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getInfoWindow(p0: Marker?): View? {
        val infoWindowView = layoutInflater.inflate(R.layout.infowindow_custom,null)
        if(p0 != null){
            infoWindowView.infowindow_name.text = p0.title
            infoWindowView.infowindow_details.text = p0.snippet
        }
        return infoWindowView
    }

    override fun getInfoContents(p0: Marker?): View? {
        return null
    }
}