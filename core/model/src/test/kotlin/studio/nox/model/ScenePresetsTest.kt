package studio.nox.model

import org.junit.Assert.*
import org.junit.Test

class ScenePresetsTest {
    @Test fun eachNewSceneHasThreeNamedStyles() {
        val names=mapOf(
            "AURORA" to listOf("Polar","Emerald","Violet Mist"),
            "PULSE" to listOf("Cyan Beat","Crimson Core","Neon Echo"),
            "COMET" to listOf("Deep Space","White Tail","Blue Drift"))
        for((scene, expected) in names) {
            val all=ScenePresets.forScene(scene)
            assertEquals(expected,all.map {it.name})
            assertEquals(scene,all.first().config.sceneId)
            assertEquals(3,all.size)
            assertTrue(all.all {it.config.speed in 0f..1f && it.config.glow in 0f..1f && it.config.density in 0f..1f})
            assertEquals(all,ScenePresets.forScene(scene))
        }
        assertTrue(ScenePresets.forScene("UNKNOWN").isEmpty())
        assertTrue(ScenePresets.forScene("FLUX").isEmpty())
    }

    @Test fun paletteColorsMatchSpecExactly() {
        val expected=mapOf(
            "AURORA" to listOf(0x6DEBFF to 0x59FFA9,0x36F5B6 to 0x1476A9,0xAE83FF to 0x57DBFF),
            "PULSE" to listOf(0x30D9FF to 0x315EFF,0xFF648F to 0xFF8E63,0xA578FF to 0x43FFC5),
            "COMET" to listOf(0x589DFF to 0xA6CFFF,0xF1F8FF to 0x6FDFFF,0x30D9FF to 0x3952E8))
        expected.forEach { (id,pairs) ->
            assertEquals(pairs,ScenePresets.forScene(id).map { it.config.primary to it.config.secondary })
        }
    }

    @Test fun stylesDoNotMutateSourceDefaults() {
        val before=SceneCatalog.default("AURORA")
        val styles=ScenePresets.forScene("AURORA")
        assertEquals(before,SceneCatalog.default("AURORA"))
        assertNotEquals(styles[1].config,styles[0].config)
        assertEquals("AURORA",styles[1].config.normalized().sceneId)
    }

    @Test fun newScenesPortableRoundTripKeepsAllParameters() {
        val all=listOf("AURORA","PULSE","COMET").flatMap(ScenePresets::forScene)
        val original=all.map {PortablePreset(it.name,it.config)}
        assertEquals(original,PresetTransfer.decode(PresetTransfer.encode(original)))
    }
}
