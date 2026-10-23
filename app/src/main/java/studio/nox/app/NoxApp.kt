package studio.nox.app

import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import studio.nox.data.NoxPreset
import studio.nox.model.SceneCatalog
import studio.nox.model.SceneConfig
import studio.nox.renderer.MotionSensor
import studio.nox.renderer.NoxSurfaceView
import studio.nox.renderer.OffscreenExporter

private val Ink=Color(0xFF000000)
private val Surface=Color(0xFF10141C)
private val Cyan=Color(0xFF30D9FF)
private val Muted=Color(0xFF9CA9BA)
private val TextWhite=Color(0xFFF2F6FF)
private val palettes=listOf(0x30D9FF,0x8C73FF,0xFF689E,0xE0EEFF,0x45FFCF,0xFFC177)
private enum class Tab { HOME, STUDIO, LIBRARY, SETTINGS }

@Composable fun NoxApp(onApply:()->Unit,vm:NoxViewModel=viewModel()) {
    val presets by vm.presets.collectAsState()
    val settings by vm.settings.collectAsState()
    var tab by remember { mutableStateOf(Tab.HOME) }
    var homeScene by remember { mutableStateOf("FLUX") }
    var editing by remember { mutableStateOf(SceneCatalog.default("FLUX")) }
    var editId by remember { mutableLongStateOf(0) }
    var editName by remember { mutableStateOf("Blue Current") }
    var pendingExport by remember { mutableStateOf<SceneConfig?>(null) }
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    val active=presets.firstOrNull {it.id==settings.activeId}
    LaunchedEffect(active?.scene?.sceneId) { if(active!=null)homeScene=active.scene.sceneId }
    val exportLauncher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri:Uri? ->
        val snapshot=pendingExport;pendingExport=null
        if(uri==null || snapshot==null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    val metrics=context.resources.displayMetrics
                    val w=metrics.widthPixels.coerceIn(1,4096)
                    val h=metrics.heightPixels.coerceIn(1,4096)
                    val bitmap=OffscreenExporter.render(snapshot,w,h)
                    try {
                        context.contentResolver.openOutputStream(uri)?.use {
                            check(bitmap.compress(Bitmap.CompressFormat.PNG,100,it)) {"PNG compression failed"}
                        }?:error("Невозможно открыть выбранный файл")
                    } finally {bitmap.recycle()}
                }
            }.onSuccess {vm.notify("PNG сохранён")}.onFailure {vm.notify(it.message?:"Ошибка экспорта PNG")}
        }
    }
    fun openStudio(c:SceneConfig,id:Long=0,name:String="") {
        editing=c.normalized();editId=id;editName=name.ifBlank {"${c.sceneId} custom"};tab=Tab.STUDIO
    }
    fun export(c:SceneConfig) { pendingExport=c;exportLauncher.launch("NOX-${c.sceneId.lowercase()}.png") }
    fun applySafely() { runCatching {onApply()}.onFailure {vm.notify(it.message?:"Ошибка открытия системных обоев")} }
    MaterialTheme(colorScheme=darkColorScheme(primary=Cyan,onPrimary=Ink,background=Ink,onBackground=TextWhite,surface=Surface,onSurface=TextWhite)) {
        Scaffold(containerColor=Ink, bottomBar={
            NavigationBar(containerColor=Color(0xFF080B11)) {
                listOf(Tab.HOME,Tab.STUDIO,Tab.LIBRARY,Tab.SETTINGS).forEach { t ->
                    NavigationBarItem(selected=tab==t,onClick={tab=t},icon={
                        Text(when(t){Tab.HOME->"◇";Tab.STUDIO->"✦";Tab.LIBRARY->"▦";Tab.SETTINGS->"⚙"},fontSize=21.sp)
                    },label={Text(when(t){Tab.HOME->"Главная";Tab.STUDIO->"Студия";Tab.LIBRARY->"Коллекция";Tab.SETTINGS->"Настройки"},fontSize=10.sp)},alwaysShowLabel=true)
                }
            }
        }) { insets ->
            Column(Modifier.fillMaxSize().padding(insets).background(Ink)) {
                Row(Modifier.fillMaxWidth().padding(start=22.dp,end=22.dp,top=14.dp,bottom=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                    Column {
                        Text("NOX",fontSize=29.sp,fontWeight=FontWeight.Black,color=TextWhite,letterSpacing=6.sp)
                        Text("LIVING LIGHT STUDIO",fontSize=9.sp,color=Cyan,letterSpacing=2.sp)
                    }
                    Text("1.0",fontSize=12.sp,color=Muted)
                }
                when(tab) {
                    Tab.HOME -> HomeScreen(homeScene,active,settings.fps,settings.gyroEnabled,
                        onScene={homeScene=it},onEdit={openStudio(it,active?.takeIf{p->p.scene==it}?.id?:0,active?.takeIf{p->p.scene==it}?.name?:"")},
                        onApply={
                            if(active?.scene?.sceneId==homeScene) applySafely()
                            else vm.saveAndApply("${homeScene} default", if(active?.scene?.sceneId==homeScene) active.scene else SceneCatalog.default(homeScene)) {applySafely()}
                        },onExport={export(it)})
                    Tab.STUDIO -> StudioScreen(editing,editName,settings.fps,settings.gyroEnabled,
                        onEdit={editing=it},onName={editName=it},onSave={vm.save(editName,editing,editId) { editId=it }},
                        onExport={export(editing)},onApply={
                            vm.saveAndApply(editName,editing,editId,onSaved={editId=it}) {applySafely()}
                        })
                    Tab.LIBRARY -> LibraryScreen(presets,settings.activeId,
                        onEdit={openStudio(it.scene,it.id,it.name)},onUse={vm.use(it)},
                        onDuplicate={vm.duplicate(it)},onDelete={vm.delete(it)})
                    Tab.SETTINGS -> SettingsScreen(settings.fps,settings.gyroEnabled,vm::setFps,vm::setGyro)
                }
            }
            vm.message?.let { message ->
                AlertDialog(onDismissRequest={vm.notify(null)},confirmButton={TextButton(onClick={vm.notify(null)}){Text("Понятно")}},title={Text("NOX")},text={Text(message)})
            }
        }
    }
}

@Composable private fun HomeScreen(scene:String,active:NoxPreset?,fps:Int,gyro:Boolean,onScene:(String)->Unit,onEdit:(SceneConfig)->Unit,onApply:()->Unit,onExport:(SceneConfig)->Unit) {
    val c=if(active?.scene?.sceneId==scene)active.scene else SceneCatalog.default(scene)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp)) {
        Row(Modifier.padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("LIGHT IN MOTION",fontSize=11.sp,letterSpacing=2.sp,color=Cyan)
                Spacer(Modifier.height(4.dp))
                Text("Создавай свет.",fontSize=25.sp,fontWeight=FontWeight.SemiBold,color=TextWhite)
            }
            Text("01 / 03",fontSize=12.sp,color=Muted)
        }
        Box(Modifier.fillMaxWidth().height(390.dp).clip(RoundedCornerShape(22.dp)).background(Ink).border(1.dp,Color(0xFF202D3C),RoundedCornerShape(22.dp))) {
            LivePreview(c,fps,gyro,Modifier.fillMaxSize())
            Column(Modifier.align(Alignment.TopStart).padding(18.dp)) {
                Text(scene,fontSize=13.sp,letterSpacing=3.sp,color=TextWhite,fontWeight=FontWeight.Bold)
                Text("REAL-TIME GENERATIVE LIGHT",fontSize=9.sp,color=Muted,letterSpacing=1.sp)
            }
            Text("TOUCH TO INTERACT",modifier=Modifier.align(Alignment.BottomStart).padding(18.dp),fontSize=9.sp,letterSpacing=1.sp,color=Muted)
        }
        Spacer(Modifier.height(15.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            SceneCatalog.ids.forEach { id ->
                FilterChip(selected=id==scene,onClick={onScene(id)},label={Text(id,fontSize=12.sp)})
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Button(onClick={onEdit(c)},modifier=Modifier.weight(1f).height(50.dp),shape=RoundedCornerShape(13.dp)) { Text("Открыть студию") }
            OutlinedButton(onClick=onApply,modifier=Modifier.weight(1f).height(50.dp),shape=RoundedCornerShape(13.dp)) { Text("Установить") }
        }
        TextButton(onClick={onExport(c)},modifier=Modifier.align(Alignment.End)) { Text("Экспорт PNG ↗") }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable private fun StudioScreen(config:SceneConfig,name:String,fps:Int,gyro:Boolean,onEdit:(SceneConfig)->Unit,onName:(String)->Unit,onSave:()->Unit,onExport:()->Unit,onApply:()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp)) {
        Text("РЕДАКТОР СЦЕНЫ",fontSize=11.sp,color=Cyan,letterSpacing=2.sp)
        Spacer(Modifier.height(11.dp))
        Box(Modifier.fillMaxWidth().height(310.dp).clip(RoundedCornerShape(20.dp)).border(1.dp,Color(0xFF1C2937),RoundedCornerShape(20.dp))) {
            LivePreview(config,fps,gyro,Modifier.fillMaxSize())
            Text(config.sceneId,Modifier.align(Alignment.TopStart).padding(16.dp),fontSize=16.sp,color=TextWhite,letterSpacing=3.sp)
        }
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(name,onName,label={Text("Название пресета")},singleLine=true,modifier=Modifier.fillMaxWidth(),isError=name.trim().isEmpty()||name.trim().length>48)
        Spacer(Modifier.height(18.dp))
        Text("ОСНОВНОЙ ЦВЕТ",color=Muted,fontSize=11.sp,letterSpacing=2.sp)
        PaletteChoices(config.primary) { onEdit(config.copy(primary=it)) }
        Text("ДОПОЛНИТЕЛЬНЫЙ ЦВЕТ",color=Muted,fontSize=11.sp,letterSpacing=2.sp)
        PaletteChoices(config.secondary) { onEdit(config.copy(secondary=it)) }
        Parameter("Скорость",config.speed){onEdit(config.copy(speed=it))}
        Parameter("Свечение",config.glow){onEdit(config.copy(glow=it))}
        Parameter("Плотность",config.density){onEdit(config.copy(density=it))}
        Parameter("Параллакс",config.motion){onEdit(config.copy(motion=it))}
        Spacer(Modifier.height(13.dp))
        Button(onClick=onSave,enabled=name.trim().isNotBlank()&&name.trim().length<=48,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp)) {Text("Сохранить пресет")}
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.fillMaxWidth()) {
            TextButton(onClick=onExport,modifier=Modifier.weight(1f)) {Text("Экспорт PNG")}
            TextButton(onClick=onApply,modifier=Modifier.weight(1f)) {Text("Установить обои")}
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable private fun PaletteChoices(selected:Int,onSelect:(Int)->Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical=11.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        palettes.forEach { color ->
            Box(Modifier.size(41.dp).border(if(selected==color)3.dp else 1.dp,if(selected==color)TextWhite else Color.DarkGray,CircleShape).padding(5.dp)
                .background(Color(0xFF000000.toInt() or color),CircleShape).clip(CircleShape).clickable {onSelect(color)})
        }
    }
}

@Composable private fun Parameter(name:String,value:Float,onValueChange:(Float)->Unit) {
    Column {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(name,color=TextWhite,fontSize=14.sp)
            Text("${(value*100).toInt()}%",color=Cyan,fontSize=13.sp)
        }
        Slider(value=value,onValueChange=onValueChange,valueRange=0f..1f,modifier=Modifier.fillMaxWidth())
    }
}

@Composable private fun LibraryScreen(presets:List<NoxPreset>,active:Long?,onEdit:(NoxPreset)->Unit,onUse:(NoxPreset)->Unit,onDuplicate:(NoxPreset)->Unit,onDelete:(NoxPreset)->Unit) {
    var confirming by remember {mutableStateOf<NoxPreset?>(null)}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp)) {
        Text("ТВОИ КОМПОЗИЦИИ",color=Cyan,fontSize=11.sp,letterSpacing=2.sp)
        Spacer(Modifier.height(15.dp))
        if(presets.isEmpty()) Text("Пока нет сохранённых пресетов. Создай первый в Студии.",color=Muted)
        presets.forEach { preset ->
            Row(Modifier.fillMaxWidth().padding(bottom=10.dp).background(Surface,RoundedCornerShape(17.dp)).padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
                Miniature(preset.scene,Modifier.size(width=86.dp,height=102.dp).clip(RoundedCornerShape(12.dp)).background(Ink))
                Column(Modifier.weight(1f).padding(start=13.dp)) {
                    Text(preset.name,fontWeight=FontWeight.SemiBold,color=TextWhite,fontSize=15.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text(preset.scene.sceneId,color=Muted,fontSize=11.sp)
                    if(preset.id==active) Text("● Активный",color=Cyan,fontSize=11.sp)
                    Row(horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        TextButton(onClick={onEdit(preset)},contentPadding=PaddingValues(4.dp)){Text("Править",fontSize=11.sp)}
                        TextButton(onClick={onUse(preset)},contentPadding=PaddingValues(4.dp)){Text("Выбрать",fontSize=11.sp)}
                    }
                    Row(horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        TextButton(onClick={onDuplicate(preset)},contentPadding=PaddingValues(4.dp)){Text("Копия",fontSize=11.sp)}
                        TextButton(onClick={confirming=preset},contentPadding=PaddingValues(4.dp)){Text("Удалить",fontSize=11.sp,color=Color(0xFFFF8392))}
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
    confirming?.let { preset ->
        AlertDialog(onDismissRequest={confirming=null},title={Text("Удалить пресет?")},text={Text("«${preset.name}» будет удалён без возможности восстановления.")},
            confirmButton={TextButton(onClick={onDelete(preset);confirming=null}){Text("Удалить")}},
            dismissButton={TextButton(onClick={confirming=null}){Text("Отмена")}})
    }
}

@Composable private fun Miniature(config:SceneConfig,modifier:Modifier=Modifier) {
    Canvas(modifier) {
        val c=Color(0xFF000000.toInt() or config.primary)
        for (i in 0..3) {
            val path=Path().apply {
                moveTo(-size.width*0.18f,size.height*(0.75f-i*0.07f))
                cubicTo(size.width*0.7f,size.height*(0.85f-i*0.22f),size.width*0.1f,size.height*0.12f,size.width*1.2f,size.height*(0.15f+i*0.04f))
            }
            drawPath(path,color=c.copy(alpha=0.1f),style=Stroke(width=13f))
            drawPath(path,color=c.copy(alpha=0.75f),style=Stroke(width=1.8f))
        }
    }
}

@Composable private fun SettingsScreen(fps:Int,gyro:Boolean,onFps:(Int)->Unit,onGyro:(Boolean)->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp)) {
        Text("ПАРАМЕТРЫ NOX",fontSize=11.sp,color=Cyan,letterSpacing=2.sp)
        Spacer(Modifier.height(20.dp))
        Text("Производительность",color=TextWhite,fontSize=23.sp,fontWeight=FontWeight.SemiBold)
        Text("Целевая частота кадров. Реальная зависит от Android, энергосбережения и нагрева.",color=Muted,fontSize=13.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            listOf(30,60,120).forEach { cap ->
                FilterChip(selected=fps==cap,onClick={onFps(cap)},label={Text("$cap FPS")})
            }
        }
        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color=Color(0xFF24303B))
        Row(Modifier.fillMaxWidth().padding(vertical=18.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Движение устройства",color=TextWhite,fontSize=17.sp)
                Text("Параллакс на основе датчика гравитации",color=Muted,fontSize=12.sp)
            }
            Switch(checked=gyro,onCheckedChange=onGyro)
        }
        HorizontalDivider(color=Color(0xFF24303B))
        Spacer(Modifier.height(24.dp))
        Text("Без интернета, без учётной записи и фонового трекинга.",color=Muted,fontSize=13.sp)
        Text("При скрытии обоев GPU-рендеринг и датчик движения отключаются.",color=Muted,fontSize=13.sp,modifier=Modifier.padding(top=9.dp))
    }
}

@Composable private fun LivePreview(config:SceneConfig,fps:Int,gyro:Boolean,modifier:Modifier=Modifier) {
    val context=LocalContext.current
    val view=remember(context){NoxSurfaceView(context)}
    var err by remember {mutableStateOf<String?>(null)}
    view.onError = { message: String -> err=message }
    LifecycleResumeEffect(view,gyro) {
        view.resumeRender()
        val sensor=MotionSensor(context){x,y->view.tilt(x,y)}
        if(gyro) sensor.start() else view.tilt(0f,0f)
        onPauseOrDispose {sensor.stop();view.pauseRender()}
    }
    Box(modifier.background(Ink)) {
        AndroidView(factory={view},update={it.configure(config,fps)},modifier=Modifier.fillMaxSize())
        if(err!=null) Text("OpenGL: $err",color=Color.Red,modifier=Modifier.align(Alignment.Center).padding(12.dp))
    }
}
