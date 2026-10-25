package dev.lolokeksu.contextcapsule

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.lolokeksu.contextcapsule.data.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class MemoryMigrationTest {
    @Test fun migrationPreservesExistingDecisionAndDefaultsToUnpinned() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-${UUID.randomUUID()}.db"
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE decisions (" +
                        "id TEXT NOT NULL PRIMARY KEY, projectId TEXT NOT NULL, " +
                        "text TEXT NOT NULL, evidence TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                    db.execSQL("INSERT INTO decisions VALUES ('n1','p1','Сохранённое решение','OWNER_CONFIRMED',100)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            }).build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        try {
            val db = helper.writableDatabase
            MIGRATION_1_2.migrate(db)
            db.query("SELECT text, evidence, pinned FROM decisions WHERE id='n1'").use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals("Сохранённое решение", cursor.getString(0))
                assertEquals("OWNER_CONFIRMED", cursor.getString(1))
                assertEquals(0, cursor.getInt(2))
            }
        } finally {
            helper.close()
            context.deleteDatabase(name)
        }
    }
}
