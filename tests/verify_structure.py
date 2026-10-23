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
    def test_room_supertype_visible_to_consuming_android_modules(self):
        data=self.read('core/data/build.gradle.kts')
        self.assertIn('api("androidx.room:room-runtime:2.8.5")', data,
            'NoxDatabase publicly inherits RoomDatabase; its dependency must be api, not implementation')
    def test_studio_preview_remains_visible_while_controls_scroll(self):
        source = self.read('app/src/main/java/studio/nox/app/NoxApp.kt')
        studio = source.split('@Composable private fun StudioScreen(', 1)[1].split('@Composable private fun FullscreenPreview(', 1)[0]
        self.assertIn('BoxWithConstraints(Modifier.fillMaxSize())', studio)
        self.assertIn('Column(Modifier.fillMaxSize().padding(horizontal=18.dp))', studio)
        pinned_preview = studio.index('LivePreview(displayConfig,fps,gyro,Modifier.fillMaxSize())')
        scrollable_controls = studio.index('Column(Modifier.weight(1f).verticalScroll(rememberScrollState()))')
        self.assertLess(pinned_preview, scrollable_controls)
        self.assertEqual(studio.count('verticalScroll(rememberScrollState())'), 1,
                         'Only editing controls must scroll, not the preview or header')
        self.assertIn('previewHeight', studio)
        self.assertIn('FullscreenPreview(config,fps,gyro', studio)

    def test_palette_lab_and_pinned_preview_tools(self):
        ui=self.read('app/src/main/java/studio/nox/app/NoxApp.kt')
        studio=ui.split('@Composable private fun StudioScreen(',1)[1].split('@Composable private fun FullscreenPreview(',1)[0]
        for item in ('ColorHarmony.Mode.entries', 'ColorHarmony.apply(config, mode)',
                     'compareOriginal', 'compactPreview', 'Оригинал',
                     'previewHeight = if (compactPreview)', 'LivePreview(displayConfig'):
            self.assertIn(item,studio)
        self.assertIn('Column(Modifier.weight(1f).verticalScroll(rememberScrollState()))',studio)
        self.assertEqual(studio.count('verticalScroll(rememberScrollState())'),1)
        self.assertLess(studio.index('LivePreview(displayConfig'),studio.index('Column(Modifier.weight(1f).verticalScroll(rememberScrollState()))'))

    def test_premium_studio_11_tools(self):
        ui=self.read('app/src/main/java/studio/nox/app/NoxApp.kt')
        for symbol in ['SceneLooks.looks', 'SceneLooks.shuffle(', 'FullscreenPreview(',
                       'SceneCatalog.fromHex(', 'Поиск по пресетам', 'SearchableLibrary']:
            self.assertIn(symbol, ui, 'NOX 1.1 must expose real editor or discovery functionality: ' + symbol)
    def test_app_upgrades_existing_install_in_place(self):
        app=self.read('app/build.gradle.kts')
        for contract in ['applicationId = "studio.nox.light"', 'applicationIdSuffix = ".previewlab"', 'versionCode = 4',
                         'versionName = "1.2.0"']:
            self.assertIn(contract, app)
    def test_build_workflow_creates_apk(self):
        workflow=self.read('.github/workflows/android.yml')
        self.assertIn('gradle :app:assembleDebug',workflow)
        self.assertIn('app/build/outputs/apk/debug/app-debug.apk',workflow)

if __name__=='__main__':unittest.main(verbosity=2)
