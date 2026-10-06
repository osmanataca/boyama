package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.ArtworkProgressCodec
import com.example.model.FillTexture
import com.example.model.RegionFillState
import com.example.model.VectorArtCatalog
import com.example.ui.canvas.CanvasHitTester
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `launch MainActivity and navigate all tabs and open coloring studio`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ayşe'nin Resim Atölyesi", appName)

        // Verify Home Atolye Screen renders without crash
        composeTestRule.onNodeWithTag("home_atolye_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("daily_featured_card").assertIsDisplayed()

        // Navigate to Gallery tab
        composeTestRule.onNodeWithTag("nav_tab_gallery").performClick()
        composeTestRule.onNodeWithTag("gallery_screen").assertIsDisplayed()

        // Navigate to Palettes tab
        composeTestRule.onNodeWithTag("nav_tab_palettes").performClick()
        composeTestRule.onNodeWithTag("palettes_screen").assertIsDisplayed()

        // Navigate to Achievements tab
        composeTestRule.onNodeWithTag("nav_tab_achievements").performClick()
        composeTestRule.onNodeWithTag("achievements_screen").assertIsDisplayed()

        // Return to Studio tab and open the daily featured artwork into ColoringStudioScreen
        composeTestRule.onNodeWithTag("nav_tab_studio").performClick()
        composeTestRule.onNodeWithTag("start_daily_artwork_button").performClick()
        composeTestRule.onNodeWithTag("coloring_studio_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("interactive_coloring_canvas").assertIsDisplayed()
    }

    @Test
    fun `verify vector catalog and hit tester and codec`() {
        val templates = VectorArtCatalog.allTemplates
        assertEquals(10, templates.size)

        val mandala = VectorArtCatalog.findById("anatolian_mandala")
        val centerHit = CanvasHitTester.findRegionAt(mandala, 500f, 500f)
        assertNotNull(centerHit)
        assertEquals("Güneş Çekirdeği", centerHit?.nameTr)

        val original = mapOf(
            1 to RegionFillState(1, 0xFFE85D4AL, FillTexture.WATERCOLOR),
            2 to RegionFillState(2, 0xFF2A9D8FL, FillTexture.GRADIENT)
        )
        val encoded = ArtworkProgressCodec.encodeFills(original)
        val decoded = ArtworkProgressCodec.decodeFills(encoded)
        assertEquals(original, decoded)
        assertTrue(templates.all { it.regions.size >= 15 })
    }
}
