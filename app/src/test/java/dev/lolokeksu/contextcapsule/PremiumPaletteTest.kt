package dev.lolokeksu.contextcapsule

import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumPaletteTest {
    @Test fun heroTitleIsReadableOnTrueBlack() {
        assertTrue(PremiumPalette.contrastRatio(PremiumPalette.Title, PremiumPalette.Background) >= 7.0)
    }

    @Test fun secondaryTextIsReadableOnCards() {
        assertTrue(PremiumPalette.contrastRatio(PremiumPalette.Secondary, PremiumPalette.Panel) >= 4.5)
    }
}
