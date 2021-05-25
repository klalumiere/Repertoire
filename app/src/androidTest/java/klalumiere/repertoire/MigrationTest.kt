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
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2() {
        var db = helper.createDatabase(testDbName, 1).apply {
            // Cannot use DAO classes because they expect the latest schema.
            execSQL("INSERT INTO song(uri, name) VALUES ('file://test','Pearl Jam - Black');")
            val cursor = query("SELECT * FROM song")
            assertEquals(1, cursor.count)
            cursor.close()
            close()
        }
        
//        db = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }

    private val testDbName = "migration-test"
}