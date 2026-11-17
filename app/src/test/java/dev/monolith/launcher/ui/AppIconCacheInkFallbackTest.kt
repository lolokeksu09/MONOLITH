package dev.monolith.launcher.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests the exact bitmap acceptance/fallback boundary, not only a preference
 * setting. Physical app-specific adaptive icons still require Lenovo QA.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppIconCacheInkFallbackTest {
    @Test fun transparentNativeMonochromeFallsBackToUnalteredSource() {
        val result = renderInkWithOriginalFallback(
            ColorDrawable(Color.RED),
            ColorDrawable(Color.TRANSPARENT),
            64
        )
        assertEquals(Color.RED, result.getPixel(32, 32))
    }

    @Test fun missingNativeMonochromePreservesExistingOriginalFallback() {
        val result = renderInkWithOriginalFallback(
            ColorDrawable(Color.BLUE),
            null,
            64
        )
        assertEquals(Color.BLUE, result.getPixel(32, 32))
    }

    @Test fun visibleNativeMonochromeRetainsInkTint() {
        val result = renderInkWithOriginalFallback(
            ColorDrawable(Color.RED),
            ColorDrawable(Color.WHITE),
            64
        )
        assertTrue(containsVisibleInkArtwork(result))
        assertEquals(0xFFF4F3F0.toInt(), result.getPixel(32, 32))
    }

    @Test fun barelyVisibleNoiseDoesNotPassAsValidInkArtwork() {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        assertFalse(containsVisibleInkArtwork(bitmap))
        bitmap.setPixel(32, 32, Color.WHITE)
        assertFalse(containsVisibleInkArtwork(bitmap))
        for (x in 20..28) for (y in 20..28) bitmap.setPixel(x, y, Color.WHITE)
        assertTrue(containsVisibleInkArtwork(bitmap))
    }

    @Test fun almostTransparentMonochromeIsNotRecognizable() {
        val result = renderInkWithOriginalFallback(
            ColorDrawable(Color.GREEN),
            ColorDrawable(Color.argb(4, 255, 255, 255)),
            64
        )
        assertEquals(Color.GREEN, result.getPixel(32, 32))
    }
}
