package com.example.nunya.letsmakeapicnic

/**
 * Created by Stewart Collins on 11/01/18.
 */
data class PlaceDetails (
        val name: String,
        val opening_hours: OpeningHoursInfo?

)

data class OpeningHoursInfo (
        val open_now: Boolean,
        val weekday_text: Array<String>
)

data class PlaceDetailsResult(
        val result: PlaceDetails
)