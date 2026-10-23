package studio.nox.model

import org.junit.Assert.*
import org.junit.Test

class ColorHarmonyTest {
    @Test fun allHarmoniesAreDeterministicAndSafeRgb() {
        val start = SceneCatalog.default("HALO").copy(primary = 0xFFAA00)
        for (mode in ColorHarmony.Mode.entries) {
            val actual = ColorHarmony.apply(start, mode)
            assertEquals(actual, ColorHarmony.apply(start, mode))
            assertTrue(actual.secondary in 0..0xFFFFFF)
            assertNotEquals(start.primary, actual.secondary)
        }
    }

    @Test fun harmoniesPreserveUnrelatedSceneParameters() {
        val start = SceneConfig("NEBULA", primary=0x3688D1, secondary=0x004488,
            speed=0.22f, glow=0.8f, density=0.4f, motion=0.6f, seed=543)
        val result = ColorHarmony.apply(start, ColorHarmony.Mode.COMPLEMENTARY)
        assertEquals(start.sceneId, result.sceneId)
        assertEquals(start.primary, result.primary)
        assertEquals(start.speed, result.speed, 0f)
        assertEquals(start.glow, result.glow, 0f)
        assertEquals(start.density, result.density, 0f)
        assertEquals(start.motion, result.motion, 0f)
        assertEquals(start.seed, result.seed)
    }

    @Test fun grayscaleColorStillProducesVisibleColorHarmony() {
        val start = SceneConfig("FLUX", primary=0x777777)
        val output = ColorHarmony.apply(start, ColorHarmony.Mode.TRIADIC)
        val r = (output.secondary shr 16) and 255
        val g = (output.secondary shr 8) and 255
        val b = output.secondary and 255
        assertTrue(maxOf(r,g,b)-minOf(r,g,b) >= 25)
    }

    @Test fun adjacentAndOppositeHarmonyAreDifferent() {
        val start = SceneConfig("FLUX",primary=0x2080FF)
        val a = ColorHarmony.apply(start, ColorHarmony.Mode.ANALOGOUS)
        val b = ColorHarmony.apply(start, ColorHarmony.Mode.COMPLEMENTARY)
        val c = ColorHarmony.apply(start, ColorHarmony.Mode.TRIADIC)
        assertNotEquals(a.secondary,b.secondary)
        assertNotEquals(b.secondary,c.secondary)
        assertNotEquals(a.secondary,c.secondary)
    }
}
