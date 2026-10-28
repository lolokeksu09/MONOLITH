package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetSizeTest {
    @Test fun boundsPreventWidgetsFromOccupyingUnboundedDesktopSpace() {
        assertEquals(128, normalizeWidgetHeight(Int.MIN_VALUE))
        assertEquals(192, normalizeWidgetHeight(192))
        assertEquals(512, normalizeWidgetHeight(Int.MAX_VALUE))
    }

    @Test fun widgetHeightUsesPredictableStepsAndStopsAtBounds() {
        assertEquals(256, stepWidgetHeight(192, +1))
        assertEquals(128, stepWidgetHeight(192, -1))
        assertEquals(512, stepWidgetHeight(512, +1))
        assertEquals(128, stepWidgetHeight(128, -1))
        assertEquals(256, stepWidgetHeight(220, +1))
        assertEquals(192, stepWidgetHeight(220, -1))
    }

    @Test fun existingWidgetsStartAtOldApproximateSize() {
        assertEquals(192, DEFAULT_WIDGET_HEIGHT_DP)
    }

    @Test fun disabledSearchCannotHideTheAppGridWithInvisibleQuery() {
        assertEquals("", effectiveDrawerQuery("GitHub", false))
        assertEquals("GitHub", effectiveDrawerQuery("GitHub", true))
    }
}
