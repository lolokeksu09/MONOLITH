package dev.lolokeksu.contextcapsule

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.lolokeksu.contextcapsule.data.DecisionRow
import dev.lolokeksu.contextcapsule.data.ProjectDatabase
import dev.lolokeksu.contextcapsule.data.ProjectRow
import dev.lolokeksu.contextcapsule.data.TaskRow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkspaceMetricsTest {
    @Test fun projectStatsAreIsolatedAndRemoteProjectUpdatesCannotLeak() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, ProjectDatabase::class.java).build()
        try {
            val dao = database.dao()
            dao.upsertProject(ProjectRow("first","First",null,1))
            dao.upsertProject(ProjectRow("second","Second",null,2))
            dao.upsertTask(TaskRow("t1","first","Waiting",false,1))
            dao.upsertTask(TaskRow("t2","first","Done",true,2))
            dao.upsertTask(TaskRow("t3","second","Private task",false,3))
            dao.upsertDecision(DecisionRow("n1","first","Needs review","UNVERIFIED",1))
            dao.upsertDecision(DecisionRow("n2","second","Approved","OWNER_CONFIRMED",2))
            val metrics = dao.projectMetrics().associateBy { it.projectId }
            assertEquals(2, metrics.size)
            assertEquals(1, metrics["first"]?.openTasks)
            assertEquals(1, metrics["first"]?.completedTasks)
            assertEquals(1, metrics["first"]?.pendingNotes)
            assertEquals(0, metrics["second"]?.pendingNotes)
            assertEquals(1, metrics["second"]?.confirmedNotes)
            assertEquals(0, dao.confirmDecision("second","n1"))
            assertEquals(0, dao.toggleTask("first","t3",true))
            assertEquals(1, dao.toggleTask("second","t3",true))
        } finally {
            database.close()
        }
    }
}
