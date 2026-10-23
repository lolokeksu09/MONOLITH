"""Offline consistency checks only; does NOT assert Android build or device correctness."""
import pathlib
import re
import unittest
import xml.etree.ElementTree as ET

ROOT=pathlib.Path(__file__).resolve().parent.parent

class StructuralContracts(unittest.TestCase):
    def read(self,path):return (ROOT/path).read_text(encoding='utf-8')
    def test_no_internet_or_storage_permissions(self):
        for path in ROOT.glob('**/AndroidManifest.xml'):
            text=path.read_text(encoding='utf-8')
            self.assertNotIn('<uses-permission',text)
            ET.fromstring(text)
    def test_module_layout(self):
        root=self.read('settings.gradle.kts')
        for name in ['app','core:model','core:renderer','core:data','feature:wallpaper']:
            self.assertIn(f'":{name}"',root)
    def test_live_wallpaper_is_exported_and_guarded(self):
        s=self.read('feature/wallpaper/src/main/AndroidManifest.xml')
        self.assertIn('android.permission.BIND_WALLPAPER',s)
        self.assertIn('android.service.wallpaper.WallpaperService',s)
        source=self.read('feature/wallpaper/src/main/java/studio/nox/wallpaper/NoxWallpaperService.kt')
        for case in ['onSurfaceCreated','onSurfaceDestroyed','onVisibilityChanged','motion.stop()','setVisible(isVisible)']:
            self.assertIn(case,source)
    def test_offline_gl_materials(self):
        shaders=self.read('core/renderer/src/main/java/studio/nox/renderer/GlBackend.kt')
        for case in ['FLUX:','HALO:','NEBULA:','GL_TRIANGLE_STRIP','gl_FragColor']:
            self.assertIn(case,shaders)
    def test_document_export_and_presets(self):
        ui=self.read('app/src/main/java/studio/nox/app/NoxApp.kt')
        self.assertIn('CreateDocument("image/png")',ui)
        self.assertIn('compress(Bitmap.CompressFormat.PNG',ui)
        repo=self.read('core/data/src/main/java/studio/nox/data/PresetEntity.kt')
        for case in ['@Database','@Dao','fun save(','fun duplicate(','fun delete(']:self.assertIn(case,repo)
    def test_compose_bom_compatible_with_compile_sdk36(self):
        app=self.read('app/build.gradle.kts')
        self.assertIn('compileSdk = 36', app)
        self.assertIn('compose-bom:2025.10.01', app,
                          'Compile SDK 36 / AGP 8.13 cannot build Compose BOM 2026.09 (Compose 1.12 needs SDK 37 / AGP 9.1)')
    def test_build_workflow_creates_apk(self):
        workflow=self.read('.github/workflows/android.yml')
        self.assertIn('gradle :app:assembleDebug',workflow)
        self.assertIn('app/build/outputs/apk/debug/app-debug.apk',workflow)

if __name__=='__main__':unittest.main(verbosity=2)
