package klalumiere.repertoire

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Rule
    @JvmField
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2() {
        val uri = "file://test"
        val songName = "Pearl Jam - Black"
        helper.createDatabase(testDbName, 1).apply {
            // Cannot use DAO classes because they expect the latest schema.
            execSQL("INSERT INTO song(uri, name) VALUES ('$uri', '$songName');")
            val cursor = query("SELECT * FROM song")
            assertEquals(1, cursor.count)
            assertEquals(2, cursor.columnCount)
            cursor.moveToNext()
            assertEquals(cursor.getString(0), uri)
            assertEquals(cursor.getString(1), songName)
            cursor.close()
            close()
        }

        val migration = AppDatabase_AutoMigration_1_2_Impl()
        helper.runMigrationsAndValidate(testDbName, 2, true, migration).apply {
            val cursor = query("SELECT * FROM song")
            assertEquals(1, cursor.count)
            assertEquals(3, cursor.columnCount)
            cursor.moveToNext()
            assertEquals(cursor.getString(0), uri)
            assertEquals(cursor.getString(1), songName)
            assertEquals(cursor.getString(2), null) // song content
            cursor.close()
            close()
        }
    }

    private val testDbName = "migration-test"
}