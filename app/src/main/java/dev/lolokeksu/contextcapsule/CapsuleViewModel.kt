package dev.lolokeksu.contextcapsule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.withTransaction
import dev.lolokeksu.contextcapsule.data.*
import dev.lolokeksu.contextcapsule.domain.*
import dev.lolokeksu.contextcapsule.github.PublicGithubClient
import dev.lolokeksu.contextcapsule.github.RepositoryOverviewCodec
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class CapsuleUiState(
    val projects: List<Project> = emptyList(),
    val active: Project? = null,
    val decisions: List<Decision> = emptyList(),
    val tasks: List<ProjectTask> = emptyList(),
    val snapshot: GithubSnapshot? = null,
    val busy: Boolean = false,
    val notice: String? = null,
    val projectStats: Map<String, WorkspaceStats> = emptyMap()
)

class CapsuleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(application, ProjectDatabase::class.java, "capsule-v1.db")
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    private val dao = db.dao()
    private val github = PublicGithubClient()
    private val _state = MutableStateFlow(CapsuleUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    private fun work(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, notice = null)
            try { block() } catch (e: Exception) {
                _state.value = _state.value.copy(notice = e.message ?: "Operation failed")
            } finally { _state.value = _state.value.copy(busy = false) }
        }
    }

    private suspend fun reload(selected: String? = _state.value.active?.id) {
        val loaded = withContext(Dispatchers.IO) {
            val projects = dao.projects().map { it.toDomain() }
            val projectStats = dao.projectMetrics().associate { row ->
                row.projectId to WorkspaceStats(
                    row.openTasks, row.completedTasks,
                    row.confirmedNotes, row.pendingNotes, row.pinnedNotes
                )
            }
            val active = projects.find { it.id == selected }
            val decisions = active?.let { dao.decisions(it.id).map(DecisionRow::toDomain) } ?: emptyList()
            val tasks = active?.let { dao.tasks(it.id).map(TaskRow::toDomain) } ?: emptyList()
            val snapshot = active?.let { dao.snapshot(it.id)?.toDomain() }
            CapsuleUiState(projects, active, decisions, tasks, snapshot, projectStats = projectStats)
        }
        _state.value = loaded.copy(busy = _state.value.busy, notice = _state.value.notice)
    }

    fun refresh() = work { reload() }
    fun open(id: String?) = work { reload(id) }

    fun create(title: String, repository: String) = work {
        val input = ProjectInput.parse(title, repository)
        val id = UUID.randomUUID().toString()
        withContext(Dispatchers.IO) {
            dao.upsertProject(ProjectRow(id, input.title, input.repository, System.currentTimeMillis()))
            requireNotNull(dao.project(id)) { "Проект не сохранился в локальной базе" }
        }
        reload(id)
        _state.value = _state.value.copy(notice = "Проект создан")
    }

    /** Editing the linked repository invalidates only its previous remote snapshot. */
    fun updateProject(title: String, repository: String) = work {
        val active = requireNotNull(_state.value.active) { "Проект не выбран" }
        val input = ProjectInput.parse(title, repository)
        withContext(Dispatchers.IO) {
            db.withTransaction {
                val current = requireNotNull(dao.project(active.id)) { "Проект не найден" }
                require(dao.updateProjectDetails(active.id, input.title, input.repository) == 1) {
                    "Не удалось изменить проект"
                }
                if (ProjectCatalog.repositoryChanged(current.repository, input.repository)) {
                    dao.deleteSnapshot(active.id)
                }
            }
        }
        reload(active.id)
        _state.value = _state.value.copy(notice = "Параметры проекта сохранены")
    }

    fun setArchive(archived: Boolean) = work {
        val id = requireNotNull(_state.value.active).id
        withContext(Dispatchers.IO) {
            require(dao.setProjectArchived(id, archived) == 1) { "Проект не найден" }
        }
        reload(null)
        _state.value = _state.value.copy(
            notice = if (archived) "Проект перенесён в архив" else "Проект восстановлен"
        )
    }

    /** Data removal requires the exact project title, also validated at the persistence boundary. */
    fun deleteActiveProject(typedName: String) = work {
        val p = requireNotNull(_state.value.active)
        require(ProjectCatalog.confirmDelete(p.title, typedName)) {
            "Для удаления введи точное название проекта"
        }
        withContext(Dispatchers.IO) {
            require(dao.deleteProject(p.id) == 1) { "Проект не найден" }
        }
        reload(null)
        _state.value = _state.value.copy(notice = "Проект удалён с устройства")
    }

    fun addDecision(text: String) = work {
        val p = requireNotNull(_state.value.active)
        val note = text.trim()
        require(note.isNotBlank() && note.length <= 20000) { "Enter a note up to 20,000 characters" }
        withContext(Dispatchers.IO) {
            dao.upsertDecision(DecisionRow(UUID.randomUUID().toString(), p.id, note, EvidenceKind.UNVERIFIED.name, System.currentTimeMillis()))
        }
        reload()
    }

    /** Only a user-confirmed destination is allowed; imported shares always start UNVERIFIED. */
    fun captureSharedText(projectId: String, raw: String) = work {
        val text = WorkspaceTools.normalizeSharedText(raw)
            ?: throw IllegalArgumentException("Текст пустой или длиннее 20 000 символов")
        withContext(Dispatchers.IO) {
            requireNotNull(dao.project(projectId)) { "Проект больше не существует" }
            dao.upsertDecision(DecisionRow(UUID.randomUUID().toString(), projectId, text,
                EvidenceKind.UNVERIFIED.name, System.currentTimeMillis()))
        }
        reload(projectId)
        _state.value = _state.value.copy(notice = "Текст сохранён как неподтверждённая заметка")
    }

    fun confirmDecision(id: String) = work {
        val projectId = requireNotNull(_state.value.active).id
        withContext(Dispatchers.IO) {
            require(dao.confirmDecision(projectId, id) == 1) { "Решение не найдено" }
        }
        reload()
    }

    fun addTask(text: String) = work {
        val p = requireNotNull(_state.value.active)
        require(text.isNotBlank() && text.length <= 500) { "Enter a task" }
        withContext(Dispatchers.IO) {
            dao.upsertTask(TaskRow(UUID.randomUUID().toString(), p.id, text.trim(), false, System.currentTimeMillis()))
        }
        reload()
    }

    fun toggleTask(id: String, done: Boolean) = work {
        val projectId = requireNotNull(_state.value.active).id
        withContext(Dispatchers.IO) {
            require(dao.toggleTask(projectId, id, done) == 1) { "Задача не найдена" }
        }
        reload()
    }

    fun editDecision(id: String, text: String) = work {
        val projectId = requireNotNull(_state.value.active).id
        val value = WorkspaceTools.normalizeSharedText(text)
            ?: error("Введите заметку длиной от 1 до 20 000 символов")
        withContext(Dispatchers.IO) {
            val prior = requireNotNull(dao.decision(projectId, id)) { "Заметка не найдена" }
            if (prior.text != value) require(dao.editDecision(projectId, id, value) == 1) { "Не удалось изменить заметку" }
        }
        reload()
    }

    fun pinDecision(id: String, pinned: Boolean) = work {
        val projectId = requireNotNull(_state.value.active).id
        withContext(Dispatchers.IO) {
            require(dao.pinDecision(projectId, id, pinned) == 1) { "Заметка не найдена" }
        }
        reload()
    }

    fun deleteDecision(id: String) = work {
        val projectId = requireNotNull(_state.value.active).id
        withContext(Dispatchers.IO) {
            require(dao.deleteDecision(projectId, id) == 1) { "Заметка не найдена" }
        }
        reload()
    }

    fun editTask(id: String, text: String) = work {
        val projectId = requireNotNull(_state.value.active).id
        val value = text.trim()
        require(value.length in 1..500) { "Введите задачу длиной от 1 до 500 символов" }
        withContext(Dispatchers.IO) {
            require(dao.editTask(projectId, id, value) == 1) { "Задача не найдена" }
        }
        reload()
    }

    fun deleteTask(id: String) = work {
        val projectId = requireNotNull(_state.value.active).id
        withContext(Dispatchers.IO) {
            require(dao.deleteTask(projectId, id) == 1) { "Задача не найдена" }
        }
        reload()
    }

    fun sync() = work {
        val p = requireNotNull(_state.value.active)
        val repo = requireNotNull(p.repository) { "Link a public GitHub repository first" }
        val previous = withContext(Dispatchers.IO) { dao.snapshot(p.id)?.toDomain() }
        val fetched = github.sync(p.id, repo, previous)
        val snap = fetched.copy(summary = RepositoryOverviewCodec.withPrevious(fetched.summary, previous))
        withContext(Dispatchers.IO) {
            dao.upsertSnapshot(SnapshotRow(snap.projectId, snap.branch, snap.sha, snap.url, snap.checkedAt, snap.summary, snap.warning))
        }
        reload()
        _state.value = _state.value.copy(notice = snap.warning?.let { "Неполная синхронизация: $it" } ?: "GitHub обновлён")
    }

    fun capsule(compact: Boolean = false): String {
        val s = _state.value
        val p = s.active ?: return ""
        val exportSnapshot = s.snapshot?.copy(summary = RepositoryOverviewCodec.readable(s.snapshot.summary))
        return CapsuleComposer().toMarkdown(CapsuleInput(p, s.decisions, s.tasks, exportSnapshot), compact)
    }

    fun backup(): String {
        val s = _state.value
        val p = requireNotNull(s.active)
        fun d(row: Decision) = JSONObject().put("text", row.text).put("status", row.evidence.name)
            .put("pinned", row.pinned)
        fun t(row: ProjectTask) = JSONObject().put("title", row.title).put("done", row.completed)
        return JSONObject().put("schemaVersion", 1)
            .put("project", JSONObject().put("title", p.title)
                .put("repository", p.repository ?: "").put("archived", p.archived))
            .put("decisions", JSONArray().apply { s.decisions.forEach { put(d(it)) } })
            .put("tasks", JSONArray().apply { s.tasks.forEach { put(t(it)) } })
            .toString(2)
    }

    fun importBackup(raw: String) = work {
        require(raw.length <= 1_000_000) { "Backup exceeds 1 MB" }
        val parsed = JSONObject(raw)
        require(parsed.getInt("schemaVersion") == 1) { "Unsupported backup format" }
        val p = parsed.getJSONObject("project")
        val title = p.getString("title").trim()
        val repo = p.optString("repository").trim().takeIf { it.isNotBlank() }
        val archived = p.optBoolean("archived", false)
        require(title.isNotEmpty() && title.length <= 100) { "Invalid project title" }
        require(repo == null || Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+").matches(repo)) { "Invalid repository" }
        val notes = parsed.getJSONArray("decisions")
        val tasks = parsed.getJSONArray("tasks")
        require(notes.length() <= 500 && tasks.length() <= 500) { "Too many entries" }
        val validNotes = (0 until notes.length()).map { i ->
            val item = notes.getJSONObject(i)
            val text = item.getString("text")
            require(text.length in 1..20000) { "Invalid note" }
            // An imported claim can never be promoted automatically.
            text to item.optBoolean("pinned", false)
        }
        val validTasks = (0 until tasks.length()).map { i ->
            val item = tasks.getJSONObject(i)
            val titleTask = item.getString("title")
            require(titleTask.length in 1..500) { "Invalid task" }
            titleTask to item.getBoolean("done")
        }
        val newId = UUID.randomUUID().toString()
        withContext(Dispatchers.IO) {
            db.withTransaction {
                dao.upsertProject(ProjectRow(newId, title, repo, System.currentTimeMillis(), archived))
                validNotes.forEach { (text, pinned) ->
                    dao.upsertDecision(DecisionRow(UUID.randomUUID().toString(), newId, text,
                        "UNVERIFIED", System.currentTimeMillis(), pinned))
                }
                validTasks.forEach { (taskTitle, done) -> dao.upsertTask(TaskRow(UUID.randomUUID().toString(), newId, taskTitle, done, System.currentTimeMillis())) }
            }
        }
        reload(newId)
        _state.value = _state.value.copy(notice = "Backup imported as a new project. Review all notes.")
    }
}
