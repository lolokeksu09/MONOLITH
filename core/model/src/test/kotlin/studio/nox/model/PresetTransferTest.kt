package studio.nox.model

import org.junit.Assert.*
import org.junit.Test

class PresetTransferTest {
    @Test fun roundTripUnicodeWithAllSceneValues() {
        val input=listOf(
            PortablePreset("Тихая туманность",SceneConfig("NEBULA",0x90AAFF,0x112233,.25f,.75f,.42f,.19f,921)),
            PortablePreset("Solar | remix",SceneConfig("HALO",0xCC2299,0x00AA34,.80f,.12f,.77f,.51f,42)))
        assertEquals(input,PresetTransfer.decode(PresetTransfer.encode(input)))
        assertEquals(PresetTransfer.encode(input),PresetTransfer.encode(input))
    }

    @Test fun rejectUnknownVersionAndTruncatedRows() {
        assertThrows(IllegalArgumentException::class.java) { PresetTransfer.decode("NOX-PRESETS/999\n") }
        assertThrows(IllegalArgumentException::class.java) { PresetTransfer.decode("NOX-PRESETS/1\nname\tFLUX") }
    }

    @Test fun rejectNonfiniteOrOutOfRangeValues() {
        val good=PresetTransfer.encode(listOf(PortablePreset("Safe",SceneCatalog.default("FLUX"))))
        assertThrows(IllegalArgumentException::class.java) { PresetTransfer.decode(good.replace("\t0.42\t","\tNaN\t")) }
        assertThrows(IllegalArgumentException::class.java) { PresetTransfer.decode(good.replace("\t0.68\t","\t6.5\t")) }
    }

    @Test fun rejectInvalidSceneAndHugeInput() {
        val good=PresetTransfer.encode(listOf(PortablePreset("Safe",SceneCatalog.default("FLUX"))))
        assertThrows(IllegalArgumentException::class.java) { PresetTransfer.decode(good.replace("\tFLUX\t","\tUNKNOWN\t")) }
        assertThrows(IllegalArgumentException::class.java) { PresetTransfer.decode("z".repeat(300_000)) }
        assertThrows(IllegalArgumentException::class.java) {
            PresetTransfer.encode((1..101).map { PortablePreset("One",SceneCatalog.default("HALO")) })
        }
    }

    @Test fun rejectUntrustedOverlongName() {
        assertThrows(IllegalArgumentException::class.java) {
            PresetTransfer.encode(listOf(PortablePreset("X".repeat(49),SceneCatalog.default("FLUX"))))
        }
    }
}
