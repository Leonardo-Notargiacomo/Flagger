package com.fontys.frontend.domain

import android.util.Log
import com.fontys.frontend.data.FlagDisplay
import com.fontys.frontend.data.PlaceService
import com.fontys.frontend.data.models.Flag
import com.fontys.frontend.data.models.FlagShowData
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException



class MapRepository { // No need for companion object if we want an instance for dependency injection
    // But if you insist on purely static, see Option 2 below.

    private val client = OkHttpClient()
    private val API_KEY = "AIzaSyA43OMJ6H8ComtRoUCLaRfMzGM2NmOMPog"

    private val FlagRepository = FlagRepository();


    suspend fun markTheSpot(latlng: LatLng): List<PlaceService> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val jsonBody = JSONObject().apply {
                    put("maxResultCount", 10)
                    put("locationRestriction", JSONObject().apply {
                        put("circle", JSONObject().apply {
                            put("center", JSONObject().apply {
                                put("latitude", latlng.latitude)
                                put("longitude", latlng.longitude)
                            })
                            put("radius", 50.0)
                        })
                    })
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("https://places.googleapis.com/v1/places:searchNearby")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Goog-Api-Key", API_KEY)
                    .addHeader(
                        "X-Goog-FieldMask",
                        "places.displayName,places.location,places.id,places.iconBackgroundColor,places.iconMaskBaseUri"
                    )
                    .post(requestBody)
                    .build()

                val call = client.newCall(request)
                continuation.invokeOnCancellation {
                    call.cancel()
                }

                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("PlacesAPI", "Request failed", e)
                        continuation.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                val errorBody = response.body?.string()
                                Log.e("PlacesAPI", "Error: ${response.code}, Body: $errorBody")
                                continuation.resumeWithException(
                                    IOException("HTTP error ${response.code}: $errorBody")
                                )
                                return
                            }

                            val responseData = response.body?.string()
                            if (responseData.isNullOrEmpty()) {
                                continuation.resume(emptyList())
                                println(responseData)
                                return
                            }
                            var placeID = ""
                            try {
                                val json = JSONObject(responseData)
                                if (!json.has("places") || json.getJSONArray("places").length() == 0) {
                                    continuation.resume(emptyList())
                                    return
                                }
                                val placesArray = json.getJSONArray("places")
                                val list = mutableListOf<PlaceService>()
                                for (i in 0 until placesArray.length()) {
                                    val place = placesArray.getJSONObject(i)
                                    val placeId = place.getString("id")
                                    val displayNameObj = place.getJSONObject("displayName")
                                    val name = displayNameObj.getString("text")
                                    val iconMaskBaseUri = place.getString("iconMaskBaseUri")
                                    val iconBackgroundColor = place.getString("iconBackgroundColor") // Corrected
                                    val locationObj = place.getJSONObject("location")
                                    val lat = locationObj.getDouble("latitude")
                                    val lng = locationObj.getDouble("longitude")
                                    val dto = PlaceService(
                                        placeId,
                                        name,
                                        iconMaskBaseUri,
                                        iconBackgroundColor,
                                        lat,
                                        lng
                                    )
                                    list.add(dto)
                                }
                                continuation.resume(list)
                            } catch (e: Exception) {
                                Log.e("PlacesAPI", "Error parsing response", e)
                                continuation.resumeWithException(e)
                            }
                        }
                    }
                })
            }
        }
    }
    suspend fun getLatlngs(list: List<String>): Result<List<FlagDisplay>> = withContext(Dispatchers.IO) {
        val deferredResults = list.map { id ->
            async {
                val request = Request.Builder()
                    .url("https://places.googleapis.com/v1/places/$id")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Goog-Api-Key", API_KEY)
                    .addHeader("X-Goog-FieldMask", "displayName,location")
                    .build()

                try {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val json = JSONObject(response.body?.string() ?: "{}")
                            val name = json.getJSONObject("displayName").getString("text")
                            val loc = json.getJSONObject("location")
                            val lat = loc.getDouble("latitude")
                            val lng = loc.getDouble("longitude")

                            FlagDisplay(name, LatLng(lat, lng),id)
                        } else {
                            val errorBody = response.body?.string()
                            Log.e("PlacesAPI", "Error fetching details for id=$id: ${response.code} - $errorBody")
                            null // Return null on HTTP error
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PlacesAPI", "Network error or parsing error for id=$id", e)
                    null // Return null on exception
                }
            }
        }

        // Wait for all concurrent calls to complete
        val results = deferredResults.awaitAll()

        val successfulFlags = results.filterNotNull()
        if (successfulFlags.isEmpty() && list.isNotEmpty()) {
            return@withContext Result.failure(Exception("Failed to fetch details for any flagged places."))
        }

        Result.success(successfulFlags)
    }

    /**
     * Fetches place names for a list of flags and merges the data to create
     * a list of FlagShowData objects.
     *
     * @param flags The input list of original Flag objects.
     * @return A Result object containing a list of FlagShowData on success,
     *         or an Exception on failure.
     */
    suspend fun getNames(flags: List<Flag>?): Result<List<FlagShowData>> = withContext(Dispatchers.IO) {
        if (flags?.isEmpty() == true) {
            return@withContext Result.success(emptyList())
        }

        // 1. Group the original flags by their locationId. This is important for
        //    merging the data back together after the API calls.
        //    We filter out any flags that don't have a locationId.
        val flagsById = flags!!.filter { it.locationId != null }.associateBy { it.locationId!! }
        val locationIds = flagsById.keys.toList()

        if (locationIds.isEmpty()) {
            return@withContext Result.success(emptyList())
        }

        // 2. Use 'async' to launch all network requests concurrently.
        //    We only need to fetch the display name, which is very efficient.
        val deferredApiResults = locationIds.map { id ->
            async {
                val request = Request.Builder()
                    .url("https://places.googleapis.com/v1/places/$id")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Goog-Api-Key", API_KEY)
                    .addHeader("X-Goog-FieldMask", "displayName") // Request ONLY the display name
                    .build()

                try {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            if (responseBody.isNullOrEmpty()) {
                                // Pair the ID with a null name to indicate failure for this item
                                return@use id to null
                            }
                            val json = JSONObject(responseBody)
                            val name = json.optJSONObject("displayName")?.optString("text", "Unknown Place")
                            // Pair the ID with its fetched name
                            id to name
                        } else {
                            Log.e("PlacesAPI", "Error fetching name for id=$id: ${response.code}")
                            id to null // Indicate failure for this item
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PlacesAPI", "Exception fetching name for id=$id", e)
                    id to null // Indicate failure for this item
                }
            }
        }

        // 3. Wait for all API calls to complete.
        val apiResults = deferredApiResults.awaitAll()

        // 4. Merge the API results with the original flag data.
        val finalShowDataList = apiResults.mapNotNull { (id, name) ->
            // Find the original flag that corresponds to this ID.
            val originalFlag = flagsById[id]

            // If the API call was successful (name is not null) and we found the original flag,
            // create the FlagShowData object.
            if (name != null && originalFlag != null) {
                FlagShowData(
                    id = originalFlag.locationId,
                    name = name, // The name comes from the API
                    photoCode = originalFlag.photoCode, // This comes from the original flag
                    dateTaken = originalFlag.dateTaken  // This also comes from the original flag
                )
            } else {
                // If the API call failed for this ID, or we can't find the original flag,
                // we exclude it from the final list.
                null
            }
        }

        // 5. Check for a total failure scenario.
        if (finalShowDataList.isEmpty() && locationIds.isNotEmpty()) {
            return@withContext Result.failure(Exception("Failed to fetch names for any of the flagged places."))
        }

        // 6. Return the successfully merged list.
        Result.success(finalShowDataList)
    }

}