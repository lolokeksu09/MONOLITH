package studio.nox.app

import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import studio.nox.model.SceneLooks
import studio.nox.renderer.MotionSensor
import studio.nox.renderer.NoxSurfaceView
import studio.nox.renderer.OffscreenExporter

private val Ink=Color(0xFF000000)
private val Surface=Color(0xFF111A26)
private val Cyan=Color(0xFF30D9FF)
private val Muted=Color(0xFF99ACC1)
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
            NavigationBar(containerColor=Color(0xFF080D13), tonalElevation=0.dp) {
                listOf(Tab.HOME,Tab.STUDIO,Tab.LIBRARY,Tab.SETTINGS).forEach { t ->
                    val selected = tab == t
                    NavigationBarItem(selected=selected,onClick={tab=t},icon={
                        Icon(imageVector=when(t){
                            Tab.HOME -> Icons.Outlined.Home
                            Tab.STUDIO -> Icons.Outlined.AutoAwesome
                            Tab.LIBRARY -> Icons.Outlined.CollectionsBookmark
                            Tab.SETTINGS -> Icons.Outlined.Tune
                        },contentDescription=null,modifier=Modifier.size(23.dp))
                    },label={Text(when(t){
                        Tab.HOME->"Обзор"
                        Tab.STUDIO->"Студия"
                        Tab.LIBRARY->"Коллекция"
                        Tab.SETTINGS->"Настройки"
                    },fontSize=11.sp)},alwaysShowLabel=true,
                        colors=NavigationBarItemDefaults.colors(
                            selectedIconColor=Cyan, selectedTextColor=TextWhite,
                            indicatorColor=Color(0xFF17303E), unselectedIconColor=Muted,
                            unselectedTextColor=Muted))
                }
            }
        }) { insets ->
            Column(Modifier.fillMaxSize().padding(insets).background(Ink)) {
                Row(Modifier.fillMaxWidth().padding(start=22.dp,end=22.dp,top=14.dp,bottom=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                    Column {
                        Text("NOX",fontSize=31.sp,fontWeight=FontWeight.Black,color=TextWhite,letterSpacing=4.sp)
                        Text("LIVING LIGHT STUDIO",fontSize=9.sp,color=Cyan,letterSpacing=2.sp)
                    }
                    Text("1.1",fontSize=12.sp,color=Muted)
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
    val config = if(active?.scene?.sceneId == scene) active.scene else SceneCatalog.default(scene)
    val subtitle = when(scene) {
        "HALO" -> "Орбиты света"
        "NEBULA" -> "Звёздная пыль"
        else -> "Световые потоки"
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top=8.dp,bottom=14.dp), verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Свет, который живёт.",fontSize=24.sp,color=TextWhite,fontWeight=FontWeight.SemiBold)
                Text("Твоя сцена. Твои правила.",fontSize=13.sp,color=Muted)
            }
            Text((SceneCatalog.ids.indexOf(scene)+1).toString().padStart(2,'0')+" / 03",
                fontSize=12.sp,color=Cyan,letterSpacing=1.sp)
        }
        Box(Modifier.fillMaxWidth().height(468.dp).clip(RoundedCornerShape(24.dp)).background(Ink)
            .border(1.dp,Color(0xFF1B3244),RoundedCornerShape(24.dp))) {
            LivePreview(config,fps,gyro,Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                0f to Color.Black.copy(alpha=0.40f),
                0.47f to Color.Transparent,
                1f to Color.Black.copy(alpha=0.92f))))
            Row(Modifier.align(Alignment.TopStart).padding(20.dp),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(Cyan,CircleShape))
                Spacer(Modifier.width(9.dp))
                Text("LIVE ENGINE",fontSize=10.sp,letterSpacing=1.5.sp,color=TextWhite)
            }
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                Text(subtitle,color=Muted,fontSize=13.sp)
                Text(scene,fontSize=42.sp,color=TextWhite,fontWeight=FontWeight.Black,letterSpacing=2.sp)
                Spacer(Modifier.height(5.dp))
                Text("OpenGL · касайся и двигай свет",fontSize=12.sp,color=Muted)
            }
        }
        Spacer(Modifier.height(15.dp))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            SceneCatalog.ids.forEach { id ->
                FilterChip(selected=id==scene,onClick={onScene(id)},
                    label={Text(id,fontSize=12.sp,fontWeight=FontWeight.SemiBold)},
                    colors=FilterChipDefaults.filterChipColors(selectedContainerColor=Color(0xFF153544),
                        selectedLabelColor=Cyan,containerColor=Surface,labelColor=Muted))
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Button(onClick={onEdit(config)},modifier=Modifier.weight(1f).height(54.dp),
                shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Ink)) {
                Text("Создать свой",fontWeight=FontWeight.Bold)
            }
            OutlinedButton(onClick=onApply,modifier=Modifier.weight(1f).height(54.dp),
                shape=RoundedCornerShape(15.dp)) { Text("На экран",color=TextWhite) }
        }
        TextButton(onClick={onExport(config)},modifier=Modifier.align(Alignment.End)) { Text("Сохранить кадр PNG ↗",color=Cyan) }
        Spacer(Modifier.height(14.dp))
    }
}

@Composable private fun StudioScreen(config:SceneConfig,name:String,fps:Int,gyro:Boolean,onEdit:(SceneConfig)->Unit,onName:(String)->Unit,onSave:()->Unit,onExport:()->Unit,onApply:()->Unit) {
    var immersive by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp)) {
        Text("Студия света",color=TextWhite,fontSize=27.sp,fontWeight=FontWeight.SemiBold)
        Text("Форма, цвет и движение — в твоих руках.",color=Muted,fontSize=12.sp)
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(345.dp).clip(RoundedCornerShape(22.dp))
            .border(1.dp,Color(0xFF233748),RoundedCornerShape(22.dp))) {
            if(!immersive) LivePreview(config,fps,gyro,Modifier.fillMaxSize())
            Row(Modifier.align(Alignment.TopStart).padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(Cyan,CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(config.sceneId, fontSize=11.sp,color=TextWhite,letterSpacing=1.5.sp)
            }
            IconButton(onClick={immersive=true},modifier=Modifier.align(Alignment.BottomEnd).padding(12.dp)
                .background(Color(0xCC162536),CircleShape)) {
                Icon(Icons.Outlined.Fullscreen,contentDescription="На весь экран",tint=TextWhite)
            }
        }
        Spacer(Modifier.height(15.dp))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick={onEdit(SceneLooks.shuffle(config))},modifier=Modifier.weight(1f),
                shape=RoundedCornerShape(13.dp)) {
                Icon(Icons.Outlined.Casino,contentDescription=null,modifier=Modifier.size(19.dp))
                Spacer(Modifier.width(6.dp))
                Text("Новый вариант",fontSize=12.sp)
            }
            OutlinedButton(onClick={onEdit(SceneCatalog.default(config.sceneId))},
                modifier=Modifier.weight(1f),shape=RoundedCornerShape(13.dp)) {
                Icon(Icons.Outlined.RestartAlt,contentDescription=null,modifier=Modifier.size(19.dp))
                Spacer(Modifier.width(6.dp))
                Text("Сбросить",fontSize=12.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Цветовые коллекции",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=TextWhite)
        Text("Авторские сочетания для каждой сцены",fontSize=12.sp,color=Muted)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            SceneLooks.looks.forEach { look ->
                Column(Modifier.width(112.dp).clip(RoundedCornerShape(14.dp))
                    .background(Surface).clickable { onEdit(SceneLooks.apply(config, look)) }.padding(9.dp)) {
                    Box(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(9.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF000000),Color(0xFF000000.toInt() or look.secondary),
                            Color(0xFF000000.toInt() or look.primary)))))
                    Spacer(Modifier.height(8.dp))
                    Text(look.label,color=TextWhite,fontSize=12.sp,maxLines=1)
                }
            }
        }
        Spacer(Modifier.height(25.dp))
        Text("Свой стиль",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=TextWhite)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(name,onName,label={Text("Название пресета")},
            singleLine=true,modifier=Modifier.fillMaxWidth(),
            isError=name.trim().isEmpty()||name.trim().length>48,
            shape=RoundedCornerShape(14.dp))
        Spacer(Modifier.height(17.dp))
        Text("Основной цвет",color=TextWhite,fontSize=14.sp)
        PaletteChoices(config.primary) { onEdit(config.copy(primary=it)) }
        HexColorField(config.primary) { onEdit(config.copy(primary=it)) }
        Spacer(Modifier.height(18.dp))
        Text("Дополнительный цвет",color=TextWhite,fontSize=14.sp)
        PaletteChoices(config.secondary) { onEdit(config.copy(secondary=it)) }
        HexColorField(config.secondary) { onEdit(config.copy(secondary=it)) }
        Spacer(Modifier.height(20.dp))
        Text("Движение и свечение",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=TextWhite)
        Spacer(Modifier.height(12.dp))
        Parameter("Скорость",config.speed){onEdit(config.copy(speed=it))}
        Parameter("Свечение",config.glow){onEdit(config.copy(glow=it))}
        Parameter("Плотность",config.density){onEdit(config.copy(density=it))}
        Parameter("Параллакс",config.motion){onEdit(config.copy(motion=it))}
        Spacer(Modifier.height(22.dp))
        Button(onClick=onSave,enabled=name.trim().isNotBlank()&&name.trim().length<=48,
            modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(14.dp),
            colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Ink)) {
            Text("Сохранить в коллекцию",fontWeight=FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            TextButton(onClick=onExport,modifier=Modifier.weight(1f)) {Text("Экспорт PNG",color=Cyan)}
            TextButton(onClick=onApply,modifier=Modifier.weight(1f)) {Text("Установить обои",color=Cyan)}
        }
        Spacer(Modifier.height(25.dp))
    }
    if(immersive) FullscreenPreview(config,fps,gyro,onClose={immersive=false})
}

@Composable private fun FullscreenPreview(config:SceneConfig,fps:Int,gyro:Boolean,onClose:()->Unit) {
    Dialog(onDismissRequest=onClose,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Box(Modifier.fillMaxSize().background(Ink)) {
            LivePreview(config,fps,gyro,Modifier.fillMaxSize())
            Box(Modifier.fillMaxWidth().height(135.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent,Ink))))
            Row(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(24.dp),
                horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                Text("NOX / "+config.sceneId,color=TextWhite,fontSize=13.sp,letterSpacing=2.sp)
                IconButton(onClick=onClose,modifier=Modifier.background(Color(0xC51A2635),CircleShape)) {
                    Icon(Icons.Outlined.Close,contentDescription="Закрыть",tint=TextWhite)
                }
            }
            Text("Прикоснись к свету",modifier=Modifier.align(Alignment.BottomCenter).padding(28.dp),
                color=Muted,fontSize=13.sp)
        }
    }
}

@Composable private fun HexColorField(selected:Int,onSelect:(Int)->Unit) {
    var value by remember(selected) { mutableStateOf(selected.toString(16).padStart(6,'0').uppercase()) }
    val valid = SceneCatalog.fromHex(value)!=null
    OutlinedTextField(value=value,onValueChange={ typed ->
        val next=typed.removePrefix("#").take(6).uppercase()
        if(next.all { it in '0'..'9' || it in 'A'..'F' }) {
            value=next
            SceneCatalog.fromHex(next)?.let(onSelect)
        }
    },label={Text("HEX · RRGGBB")},prefix={Text("#")},singleLine=true,
        isError=!valid,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp))
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
    SearchableLibrary(presets,active,onEdit,onUse,onDuplicate,onDelete)
}

@Composable private fun SearchableLibrary(presets:List<NoxPreset>,active:Long?,onEdit:(NoxPreset)->Unit,onUse:(NoxPreset)->Unit,onDuplicate:(NoxPreset)->Unit,onDelete:(NoxPreset)->Unit) {
    var confirming by remember { mutableStateOf<NoxPreset?>(null) }
    var query by remember { mutableStateOf("") }
    val filtered = remember(presets,query) {
        presets.filter { it.name.contains(query.trim(),ignoreCase=true) ||
            it.scene.sceneId.contains(query.trim(),ignoreCase=true) }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Коллекция",color=TextWhite,fontSize=27.sp,fontWeight=FontWeight.SemiBold)
                Text("Сохранённые сцены и твои эксперименты",color=Muted,fontSize=12.sp)
            }
            Text(filtered.size.toString().padStart(2,'0'),color=Cyan,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
        }
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(value=query,onValueChange={query=it},singleLine=true,
            label={Text("Поиск по пресетам")},
            leadingIcon={Icon(Icons.Outlined.Search,contentDescription=null)},
            modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp))
        Spacer(Modifier.height(18.dp))
        if(presets.isEmpty()) {
            Text("Пока нет сохранённых пресетов. Создай первый в Студии.",color=Muted)
        } else if(filtered.isEmpty()) {
            Text("Нет совпадений. Измени запрос.",color=Muted)
        }
        filtered.forEach { preset ->
            Row(Modifier.fillMaxWidth().padding(bottom=12.dp)
                .clip(RoundedCornerShape(19.dp)).background(Surface).border(1.dp,Color(0xFF24303B),RoundedCornerShape(19.dp))
                .padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
                Miniature(preset.scene,Modifier.size(width=91.dp,height=116.dp)
                    .clip(RoundedCornerShape(13.dp)).background(Ink))
                Column(Modifier.weight(1f).padding(start=14.dp)) {
                    Text(preset.name,fontWeight=FontWeight.SemiBold,color=TextWhite,fontSize=16.sp,
                        maxLines=1,overflow=TextOverflow.Ellipsis)
                    Spacer(Modifier.height(3.dp))
                    Text(preset.scene.sceneId,color=Muted,fontSize=11.sp,letterSpacing=1.2.sp)
                    if(preset.id==active) Text("● На рабочем столе",color=Cyan,fontSize=11.sp)
                    Spacer(Modifier.height(5.dp))
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick={onEdit(preset)},contentPadding=PaddingValues(0.dp)) {
                            Text("Редактор",fontSize=12.sp,color=Cyan)
                        }
                        TextButton(onClick={onUse(preset)},contentPadding=PaddingValues(0.dp)) {
                            Text("Выбрать",fontSize=12.sp,color=TextWhite)
                        }
                    }
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick={onDuplicate(preset)},contentPadding=PaddingValues(0.dp)) {
                            Text("Копия",fontSize=12.sp,color=Muted)
                        }
                        TextButton(onClick={confirming=preset},contentPadding=PaddingValues(0.dp)) {
                            Text("Удалить",fontSize=12.sp,color=Color(0xFFFF8392))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
    confirming?.let { preset ->
        AlertDialog(onDismissRequest={confirming=null},title={Text("Удалить пресет?")},
            text={Text("«"+preset.name+"» будет удалён без возможности восстановления.")},
            confirmButton={TextButton(onClick={onDelete(preset);confirming=null}){Text("Удалить")}},
            dismissButton={TextButton(onClick={confirming=null}){Text("Отмена")}})
    }
}

@Composable private fun Miniature(config:SceneConfig,modifier:Modifier=Modifier) {
    Canvas(modifier) {
        val a=Color(0xFF000000.toInt() or config.primary)
        val b=Color(0xFF000000.toInt() or config.secondary)
        when(config.sceneId) {
            "HALO" -> {
                drawCircle(a.copy(alpha=.16f),radius=size.minDimension*.37f,
                    style=Stroke(width=15f))
                drawCircle(a.copy(alpha=.9f),radius=size.minDimension*.35f,
                    style=Stroke(width=2.5f))
                drawCircle(b.copy(alpha=.75f),radius=size.minDimension*.22f,
                    style=Stroke(width=1.5f))
                drawCircle(b.copy(alpha=.20f),radius=size.minDimension*.18f)
            }
            "NEBULA" -> {
                for(i in 0 until 29) {
                    val fx=((i*37+config.seed%97+97)%97)/97f
                    val fy=((i*59+config.seed%83+83)%83)/83f
                    val center=Offset(size.width*fx,size.height*fy)
                    drawCircle(if(i%2==0)a else b, radius=if(i%5==0)3.4f else 1.8f,
                        center=center,alpha=.7f)
                    if(i%7==0)drawCircle(a.copy(alpha=.12f),radius=12f,center=center)
                }
            }
            else -> {
                for (i in 0..3) {
                    val path=Path().apply {
                        moveTo(-size.width*.18f,size.height*(.75f-i*.07f))
                        cubicTo(size.width*.7f,size.height*(.85f-i*.22f),
                            size.width*.1f,size.height*.12f,
                            size.width*1.2f,size.height*(.15f+i*.04f))
                    }
                    drawPath(path,color=a.copy(alpha=.12f),style=Stroke(width=13f))
                    drawPath(path,color=if(i%2==0)a else b,style=Stroke(width=1.8f))
                }
            }
        }
    }
}

@Composable private fun SettingsScreen(fps:Int,gyro:Boolean,onFps:(Int)->Unit,onGyro:(Boolean)->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp)) {
        Text("Настройки",fontSize=27.sp,fontWeight=FontWeight.SemiBold,color=TextWhite)
        Text("Производительность без компромиссов с ресурсами.",fontSize=12.sp,color=Muted)
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface).padding(18.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.Speed,contentDescription=null,tint=Cyan,modifier=Modifier.size(25.dp))
                Spacer(Modifier.width(12.dp))
                Text("Частота кадров",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=TextWhite)
            }
            Spacer(Modifier.height(10.dp))
            Text("NOX адаптируется к экономии заряда и температуре устройства.",
                color=Muted,fontSize=12.sp)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf(30,60,120).forEach { cap ->
                    FilterChip(selected=fps==cap,onClick={onFps(cap)},label={Text(cap.toString()+" FPS")},
                        colors=FilterChipDefaults.filterChipColors(selectedContainerColor=Color(0xFF173B4B),
                            selectedLabelColor=Cyan,containerColor=Color(0xFF1A2430),labelColor=Muted))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface)
            .padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
            Icon(Icons.Outlined.Explore,contentDescription=null,tint=Cyan,modifier=Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Реакция на движение",color=TextWhite,fontSize=16.sp,fontWeight=FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                Text("Параллакс по датчику положения",color=Muted,fontSize=12.sp)
            }
            Switch(checked=gyro,onCheckedChange=onGyro)
        }
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF10212C),Color(0xFF0B121C))))
            .padding(20.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.Shield,contentDescription=null,tint=Cyan)
                Spacer(Modifier.width(10.dp))
                Text("Создано для приватности",fontWeight=FontWeight.SemiBold,color=TextWhite,fontSize=16.sp)
            }
            Spacer(Modifier.height(9.dp))
            Text("Без аккаунта, интернета, трекеров и рекламных SDK. Все пресеты остаются локально.",
                color=Muted,fontSize=13.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text("Когда обои скрыты, движок и датчики останавливаются. 120 FPS — верхняя цель, не гарантия.",
            color=Muted,fontSize=12.sp)
        Spacer(Modifier.height(28.dp))
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
