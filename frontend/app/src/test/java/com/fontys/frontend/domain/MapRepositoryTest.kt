package com.fontys.frontend.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
@OptIn(ExperimentalCoroutinesApi::class)
class MapRepositoryTest {

    @get:Rule

    private lateinit var repository: MapRepository

    @Before
    fun setup() {

        val client = OkHttpClient.Builder().build()

        repository = MapRepository(

        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `markTheSpot returns parsed places`() = runTest {
        // Arrange




        val result = repository.markTheSpot(
            com.google.android.gms.maps.model.LatLng(51.353528, 6.153250)
        )

        assertEquals(10, result.size)
        assertEquals("ChIJ78pbMq9ax0cRQg7zSYjwHMc", result[0].id)
        assertEquals("Fontys Venlo University of Applied Sciences", result[0].displayName)
    }
}




