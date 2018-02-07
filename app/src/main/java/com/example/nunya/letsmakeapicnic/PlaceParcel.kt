package com.example.nunya.letsmakeapicnic

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Stewart Collins on 12/01/18.
 */
class PlaceParcel(val latitude: Double,
                  val longitude: Double,
                  val name: String,
                  var openingHours: String?,
                  val type: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(name)
        parcel.writeString(openingHours)
        parcel.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object{
        val PARK = 1
        val LIQUOR_STORE = 2
        val SUPERMARKET = 3
        val SUPERMARKET_AND_LIQUOR = 4
        val CUSTOM_LOCATION = 5

        @JvmField
        val CREATOR = object : Parcelable.Creator<PlaceParcel> {
            override fun createFromParcel(parcel: Parcel): PlaceParcel {
                return PlaceParcel(parcel)
            }

            override fun newArray(size: Int): Array<PlaceParcel?> {
                return arrayOfNulls(size)
            }
        }
    }
}