package com.example.nunya.letsmakeapicnic

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

/**
 * Created by Stewart Collins on 1/02/18.
 */
data class MenuOptions (val wantsFood: Boolean,
                        val wantsDrinks: Boolean,
                        val choosePark: Boolean,
                        val startingLocation: PlaceParcel?,
                        val destination: PlaceParcel?) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readParcelable(PlaceParcel::class.java.classLoader),
            parcel.readParcelable(PlaceParcel::class.java.classLoader)) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (wantsFood) 1 else 0)
        parcel.writeByte(if (wantsDrinks) 1 else 0)
        parcel.writeByte(if (choosePark) 1 else 0)
        parcel.writeParcelable(startingLocation, flags)
        parcel.writeParcelable(destination, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val EXTRAS_STRING = "net.stewartcollins.letsmakeapicnic.menyOptions"

        @JvmField
        val CREATOR = object : Parcelable.Creator<MenuOptions> {
            override fun createFromParcel(parcel: Parcel): MenuOptions {
                return MenuOptions(parcel)
            }

            override fun newArray(size: Int): Array<MenuOptions?> {
                return arrayOfNulls(size)
            }
        }
    }
}