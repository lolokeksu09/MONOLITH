import studio.nox.model.*

fun main() {
    check(SceneCatalog.default("unknown").sceneId == "FLUX")
    check(SceneCatalog.default("HALO").sceneId == "HALO")
    check(SceneCatalog.default("NEBULA").sceneId == "NEBULA")
    check(SceneConfig(sceneId="FLUX",speed=9f,glow=-2f,density=4f,motion=-1f).normalized().speed == 1f)
    check(SceneConfig(sceneId="FLUX",speed=9f,glow=-2f,density=4f,motion=-1f).normalized().glow == 0f)
    check(SceneCatalog.validName("  ") == null)
    check(SceneCatalog.validName("  Deep cyan   ") == "Deep cyan")
    check(SceneCatalog.fromHex("#30D9FF") == 0x30D9FF)
    check(SceneCatalog.fromHex("invalid") == null)
    check(FrameBudget.allowed(120, true, 0) == 30)
    check(FrameBudget.allowed(120, false, 4) == 15)
    check(FrameBudget.allowed(120, false, 2) == 30)
    println("ModelContract: 12 assertions passed")
}
