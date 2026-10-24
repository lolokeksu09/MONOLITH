package dev.lolokeksu.contextcapsule.data

import androidx.room.*
import dev.lolokeksu.contextcapsule.domain.*

@Entity(tableName = "projects")
data class ProjectRow(@PrimaryKey val id: String, val title: String, val repository: String?, val createdAt: Long)

@Entity(tableName = "decisions", foreignKeys = [ForeignKey(entity = ProjectRow::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE)], indices = [Index("projectId")])
data class DecisionRow(@PrimaryKey val id: String, val projectId: String, val text: String, val evidence: String, val createdAt: Long)

@Entity(tableName = "tasks", foreignKeys = [ForeignKey(entity = ProjectRow::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE)], indices = [Index("projectId")])
data class TaskRow(@PrimaryKey val id: String, val projectId: String, val title: String, val completed: Boolean, val createdAt: Long)

@Entity(tableName = "snapshots", foreignKeys = [ForeignKey(entity = ProjectRow::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE)])
data class SnapshotRow(@PrimaryKey val projectId: String, val branch: String, val sha: String, val url: String, val checkedAt: Long, val summary: String, val warning: String?)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC") suspend fun projects(): List<ProjectRow>
    @Query("SELECT * FROM projects WHERE id=:id") suspend fun project(id: String): ProjectRow?
    @Query("SELECT * FROM decisions WHERE projectId=:id ORDER BY createdAt ASC") suspend fun decisions(id: String): List<DecisionRow>
    @Query("SELECT * FROM tasks WHERE projectId=:id ORDER BY createdAt ASC") suspend fun tasks(id: String): List<TaskRow>
    @Query("SELECT * FROM snapshots WHERE projectId=:id") suspend fun snapshot(id: String): SnapshotRow?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertProject(row: ProjectRow)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertDecision(row: DecisionRow)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertTask(row: TaskRow)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertSnapshot(row: SnapshotRow)
    @Query("DELETE FROM projects WHERE id=:id") suspend fun deleteProject(id: String)
    @Query("DELETE FROM decisions WHERE id=:id") suspend fun deleteDecision(id: String)
    @Query("UPDATE decisions SET evidence='OWNER_CONFIRMED' WHERE id=:id") suspend fun confirmDecision(id: String)
    @Query("UPDATE tasks SET completed=:done WHERE id=:id") suspend fun toggleTask(id: String, done: Boolean)
}

@Database(entities = [ProjectRow::class, DecisionRow::class, TaskRow::class, SnapshotRow::class], version = 1, exportSchema = false)
abstract class ProjectDatabase : RoomDatabase() { abstract fun dao(): ProjectDao }

fun ProjectRow.toDomain() = Project(id, title, repository, createdAt)
fun DecisionRow.toDomain() = Decision(id, projectId, text, EvidenceKind.entries.firstOrNull { it.name == evidence } ?: EvidenceKind.UNKNOWN, createdAt)
fun TaskRow.toDomain() = ProjectTask(id, projectId, title, completed, createdAt)
fun SnapshotRow.toDomain() = GithubSnapshot(projectId, branch, sha, url, checkedAt, summary, warning)
