package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.ShipCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Astro Dodge", appName)
    }

    @Test
    fun `verify ship catalog has starter ships`() {
        val ships = ShipCatalog.SHIPS
        assertTrue(ships.isNotEmpty())
        val scout = ShipCatalog.getShip("scout")
        assertNotNull(scout)
        assertEquals(0, scout.priceStars)
        assertTrue(scout.speedMultiplier > 0f)
    }
}
