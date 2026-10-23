package studio.nox.model
import org.junit.Assert.*
import org.junit.Test

class SceneConfigTest {
    @Test fun defaultsToFluxForUnknownId() { assertEquals("FLUX", SceneCatalog.default("bad").sceneId) }
    @Test fun boundsValues() {
        val c=SceneConfig("NEBULA", speed=-2f, density=9f).normalized()
        assertEquals(0f,c.speed,0f); assertEquals(1f,c.density,0f)
    }
    @Test fun rejectsBadNames() { assertNull(SceneCatalog.validName(" ")); assertNull(SceneCatalog.validName("x".repeat(49))) }
    @Test fun clampsThermalBudget() { assertEquals(15,FrameBudget.allowed(120,false,3)); assertEquals(30,FrameBudget.allowed(120,true,0)) }
}
