package studio.nox.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.nox.model.SceneCatalog
import studio.nox.model.SceneConfig

@Entity(tableName="presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val name:String,
    val sceneId:String,
    val primaryColor:Int,
    val secondaryColor:Int,
    val speed:Float,
    val glow:Float,
    val density:Float,
    val motion:Float,
    val seed:Int
) {
    fun toPreset()=NoxPreset(id,name,SceneConfig(sceneId,primaryColor,secondaryColor,speed,glow,density,motion,seed).normalized())
}
data class NoxPreset(val id:Long,val name:String,val scene:SceneConfig) {
    fun toEntity() = PresetEntity(id,name,scene.sceneId,scene.primary,scene.secondary,scene.speed,scene.glow,scene.density,scene.motion,scene.seed)
}
@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY id DESC") fun watchAll():Flow<List<PresetEntity>>
    @Query("SELECT * FROM presets WHERE id=:id LIMIT 1") suspend fun getById(id:Long):PresetEntity?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(entity:PresetEntity):Long
    @Query("DELETE FROM presets WHERE id=:id") suspend fun deleteById(id:Long)
}
@Database(entities=[PresetEntity::class],version=1,exportSchema=false)
abstract class NoxDatabase:RoomDatabase() {
    abstract fun presetDao():PresetDao
    companion object {
        @Volatile private var instance:NoxDatabase?=null
        fun get(context:Context):NoxDatabase=instance?:synchronized(this) {
            instance?:Room.databaseBuilder(context.applicationContext,NoxDatabase::class.java,"nox.db").build().also{instance=it}
        }
    }
}
class PresetRepository(private val dao:PresetDao) {
    val presets:Flow<List<NoxPreset>> = dao.watchAll().map { list -> list.map(PresetEntity::toPreset) }
    suspend fun get(id:Long):NoxPreset?=dao.getById(id)?.toPreset()
    suspend fun save(name:String,config:SceneConfig,id:Long=0):Long {
        val clean=SceneCatalog.validName(name)?:throw IllegalArgumentException("Название: от 1 до 48 символов")
        return dao.upsert(NoxPreset(id,clean,config.normalized()).toEntity())
    }
    suspend fun duplicate(preset:NoxPreset):Long=save("${preset.name.take(42)} copy",preset.scene)
    suspend fun delete(id:Long)=dao.deleteById(id)
}
