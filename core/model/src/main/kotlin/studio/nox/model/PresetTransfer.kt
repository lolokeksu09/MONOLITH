package studio.nox.model

import java.util.Base64

/** Versioned, offline-only interchange format for moving presets between NOX installations. */
data class PortablePreset(val name:String,val scene:SceneConfig)

object PresetTransfer {
    private const val HEADER="NOX-PRESETS/1"
    private const val MAX_PRESETS=100
    private const val MAX_CHARS=256_000
    private val hex=Regex("[0-9A-Fa-f]{6}")

    fun encode(presets:List<PortablePreset>):String {
        require(presets.size<=MAX_PRESETS) { "Не более 100 пресетов в одном файле" }
        val text=buildString {
            append(HEADER).append('\n')
            for(p in presets) {
                require(SceneCatalog.validName(p.name)==p.name) { "Некорректное название пресета" }
                val c=p.scene
                require(c.sceneId in SceneCatalog.ids) { "Неизвестный тип сцены" }
                require(listOf(c.speed,c.glow,c.density,c.motion).all { it.isFinite() && it in 0f..1f }) {
                    "Некорректные параметры сцены"
                }
                val encodedName=Base64.getUrlEncoder().withoutPadding().encodeToString(p.name.toByteArray(Charsets.UTF_8))
                append(listOf(encodedName,c.sceneId,color(c.primary),color(c.secondary),
                    c.speed.toString(),c.glow.toString(),c.density.toString(),c.motion.toString(),c.seed.toString()
                ).joinToString("\t")).append('\n')
            }
        }
        require(text.length<=MAX_CHARS) { "Файл слишком большой" }
        return text
    }

    fun decode(text:String):List<PortablePreset> {
        require(text.length<=MAX_CHARS) { "Файл слишком большой" }
        val lines=text.trimEnd('\n').split('\n')
        require(lines.firstOrNull()?.trimEnd('\r')==HEADER) { "Файл не принадлежит поддерживаемой версии NOX" }
        require(lines.size-1<=MAX_PRESETS) { "Слишком много пресетов" }
        return lines.drop(1).map { raw ->
            val fields=raw.trimEnd('\r').split('\t')
            require(fields.size==9) { "Повреждённая строка пресета" }
            val name=try {
                val bytes=Base64.getUrlDecoder().decode(fields[0])
                Charsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString()
            } catch(e:Exception) { throw IllegalArgumentException("Повреждённое название пресета",e) }
            require(SceneCatalog.validName(name)==name) { "Недопустимое название пресета" }
            val id=fields[1]
            require(id in SceneCatalog.ids) { "Неизвестный тип сцены" }
            val primary=parseColor(fields[2])
            val secondary=parseColor(fields[3])
            val values=fields.subList(4,8).map { value ->
                val f=value.toFloatOrNull()
                require(f!=null && f.isFinite() && f in 0f..1f) { "Некорректное значение параметра" }
                f
            }
            val seed=fields[8].toIntOrNull()?:throw IllegalArgumentException("Некорректный seed")
            PortablePreset(name,SceneConfig(id,primary,secondary,values[0],values[1],values[2],values[3],seed))
        }
    }
    private fun color(rgb:Int)="%06X".format(rgb and 0xFFFFFF)
    private fun parseColor(value:String):Int {
        require(hex.matches(value)) { "Некорректный цвет" }
        return value.toInt(16)
    }
}
