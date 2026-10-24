package dev.lolokeksu.contextcapsule.data

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.lolokeksu.contextcapsule.domain.*

@Entity(tableName = "projects")
data class ProjectRow(@PrimaryKey val id: String, val title: String, val repository: String?, val createdAt: Long)

@Entity(tableName = "decisions", foreignKeys = [ForeignKey(entity = ProjectRow::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE)], indices = [Index("projectId")])
data class DecisionRow(@PrimaryKey val id: String, val projectId: String, val text: String, val evidence: String, val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val pinned: Boolean = false)

@Entity(tableName = "tasks", foreignKeys = [ForeignKey(entity = ProjectRow::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE)], indices = [Index("projectId")])
data class TaskRow(@PrimaryKey val id: String, val projectId: String, val title: String, val completed: Boolean, val createdAt: Long)

@Entity(tableName = "snapshots", foreignKeys = [ForeignKey(entity = ProjectRow::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE)])
data class SnapshotRow(@PrimaryKey val projectId: String, val branch: String, val sha: String, val url: String, val checkedAt: Long, val summary: String, val warning: String?)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC") suspend fun projects(): List<ProjectRow>
    @Query("SELECT * FROM projects WHERE id=:id") suspend fun project(id: String): ProjectRow?
    @Query("SELECT * FROM decisions WHERE projectId=:id ORDER BY pinned DESC, createdAt ASC") suspend fun decisions(id: String): List<DecisionRow>
    @Query("SELECT * FROM decisions WHERE projectId=:projectId AND id=:id") suspend fun decision(projectId: String, id: String): DecisionRow?
    @Query("SELECT * FROM tasks WHERE projectId=:id ORDER BY createdAt ASC") suspend fun tasks(id: String): List<TaskRow>
    @Query("SELECT * FROM snapshots WHERE projectId=:id") suspend fun snapshot(id: String): SnapshotRow?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertProject(row: ProjectRow)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertDecision(row: DecisionRow)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertTask(row: TaskRow)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertSnapshot(row: SnapshotRow)
    @Query("DELETE FROM projects WHERE id=:id") suspend fun deleteProject(id: String)
    @Query("DELETE FROM decisions WHERE projectId=:projectId AND id=:id")
    suspend fun deleteDecision(projectId: String, id: String): Int
    @Query("UPDATE decisions SET text=:text, evidence='UNVERIFIED' WHERE projectId=:projectId AND id=:id")
    suspend fun editDecision(projectId: String, id: String, text: String): Int
    @Query("UPDATE decisions SET pinned=:pinned WHERE projectId=:projectId AND id=:id")
    suspend fun pinDecision(projectId: String, id: String, pinned: Boolean): Int
    @Query("UPDATE tasks SET title=:title WHERE projectId=:projectId AND id=:id")
    suspend fun editTask(projectId: String, id: String, title: String): Int
    @Query("DELETE FROM tasks WHERE projectId=:projectId AND id=:id")
    suspend fun deleteTask(projectId: String, id: String): Int
    @Query("UPDATE decisions SET evidence='OWNER_CONFIRMED' WHERE id=:id") suspend fun confirmDecision(id: String)
    @Query("UPDATE tasks SET completed=:done WHERE id=:id") suspend fun toggleTask(id: String, done: Boolean)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE decisions ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(entities = [ProjectRow::class, DecisionRow::class, TaskRow::class, SnapshotRow::class], version = 2, exportSchema = false)
abstract class ProjectDatabase : RoomDatabase() { abstract fun dao(): ProjectDao }

fun ProjectRow.toDomain() = Project(id, title, repository, createdAt)
fun DecisionRow.toDomain() = Decision(id, projectId, text, EvidenceKind.entries.firstOrNull { it.name == evidence } ?: EvidenceKind.UNKNOWN, createdAt, pinned)
fun TaskRow.toDomain() = ProjectTask(id, projectId, title, completed, createdAt)
fun SnapshotRow.toDomain() = GithubSnapshot(projectId, branch, sha, url, checkedAt, summary, warning)
