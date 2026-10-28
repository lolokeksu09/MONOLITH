package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetOrderTest {
    @Test fun movesWidgetsWithoutChangingTheirIds() {
        val initial = listOf(101, 202, 303)
        assertEquals(listOf(202, 101, 303), moveWidget(initial, 202, -1))
        assertEquals(listOf(101, 303, 202), moveWidget(initial, 202, 1))
        assertEquals(initial, listOf(101, 202, 303))
    }

    @Test fun cannotMoveBeyondEnds() {
        val initial = listOf(101, 202, 303)
        assertEquals(initial, moveWidget(initial, 101, -1))
        assertEquals(initial, moveWidget(initial, 303, 1))
        assertEquals(initial, moveWidget(initial, 202, 0))
    }

    @Test fun missingOrInvalidWidgetIdsAreIgnored() {
        val initial = listOf(101, 202, 303)
        assertEquals(initial, moveWidget(initial, 0, 1))
        assertEquals(initial, moveWidget(initial, 999, -1))
    }

    @Test fun persistedIdsPreserveTheirReorderedSequence() {
        assertEquals(listOf(303, 101, 202),
            parseWidgetIds(moveWidget(listOf(101, 303, 202), 303, -1).joinToString(",")))
    }
}
