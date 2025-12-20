package com.fontys.frontend.domain

import android.util.Log
import androidx.compose.runtime.internal.FunctionKeyMeta
import com.fontys.frontend.data.FlagDisplay
import com.fontys.frontend.data.PlaceService
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
import retrofit2.Retrofit
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
                            put("radius", 250.0)
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
                                    // Use optString with default and check for blank to prevent crash
                                    val iconBackgroundColor = place.optString("iconBackgroundColor", "#4285F4").let {
                                        if (it.isNullOrBlank()) "#4285F4" else it
                                    }
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
        val cords = mutableListOf<FlagDisplay>()
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
                            null
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PlacesAPI", "Network error or parsing error for id=$id", e)
                    null
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
    }