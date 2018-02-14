package net.stewartcollins.stewartcollins7.letshaveapicnic

/**These classes represent the data recieved from the GooglePlacesAPIService for place details
 * used for getting the opening hours of shops
 *
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