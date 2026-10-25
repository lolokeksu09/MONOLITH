package dev.lolokeksu.contextcapsule

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.lolokeksu.contextcapsule.data.MIGRATION_2_3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ProjectArchiveMigrationTest {
    @Test fun existingProjectAndLinkedRepositorySurviveMigrationWithActiveDefault() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "archive-migration-${UUID.randomUUID()}.db"
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(ctx).name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE projects (" +
                            "id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, " +
                            "repository TEXT, createdAt INTEGER NOT NULL)")
                        db.execSQL("INSERT INTO projects VALUES " +
                            "('p1','Legacy project','owner/repo',100)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                }).build()
        )
        try {
            val db = helper.writableDatabase
            MIGRATION_2_3.migrate(db)
            db.query("SELECT title, repository, archived FROM projects WHERE id='p1'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Legacy project", cursor.getString(0))
                assertEquals("owner/repo", cursor.getString(1))
                assertEquals(0, cursor.getInt(2))
            }
        } finally {
            helper.close()
            ctx.deleteDatabase(name)
        }
    }
}
