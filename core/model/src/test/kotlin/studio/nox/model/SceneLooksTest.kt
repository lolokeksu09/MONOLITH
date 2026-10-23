package studio.nox.model

import org.junit.Assert.*
import org.junit.Test

class SceneLooksTest {
    @Test fun hasDistinctEditorialLooks() {
        assertTrue(SceneLooks.looks.size >= 4)
        assertEquals(SceneLooks.looks.size, SceneLooks.looks.map { it.id }.distinct().size)
    }

    @Test fun applyingLookPreservesIdentityAndMotion() {
        val original = SceneConfig("HALO", speed = 0.67f, motion = 0.33f, seed = 99)
        val result = SceneLooks.apply(original, SceneLooks.looks.first())
        assertEquals("HALO", result.sceneId)
        assertEquals(0.67f, result.speed, 0f)
        assertEquals(0.33f, result.motion, 0f)
        assertEquals(99, result.seed)
        assertEquals(SceneLooks.looks.first().primary, result.primary)
    }

    @Test fun remixIsDeterministicAndNeverChangesSceneOrMotion() {
        val original = SceneConfig("NEBULA", seed=400, motion=0.72f)
        val first = SceneLooks.shuffle(original)
        assertEquals(first, SceneLooks.shuffle(original))
        assertEquals(original.sceneId, first.sceneId)
        assertEquals(original.motion, first.motion, 0f)
        assertNotEquals(original.seed, first.seed)
        assertTrue(first.speed in 0f..1f)
        assertTrue(first.glow in 0f..1f)
        assertTrue(first.density in 0f..1f)
    }

    @Test fun remixHandlesMaximumSeed() {
        val mixed = SceneLooks.shuffle(SceneConfig("FLUX", seed=Int.MAX_VALUE))
        assertTrue(mixed.seed > 0)
        assertTrue(mixed.speed.isFinite())
    }
}
