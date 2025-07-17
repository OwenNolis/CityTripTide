@file:Suppress("DEPRECATION")

package devkonig.citytriptride

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationUtils {
    /**
     * Retrieves the name of a location based on latitude and longitude.
     *
     * @param context The context to use for geocoding.
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The name of the location, or null if it could not be determined.
     */
    suspend fun getLocationName(context: Context, latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.getAddressLine(0)
            } catch (_: Exception) {
                null
            }
        }
    }
}