package ir.mabhas19.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseRoundTripTest {
    @Test fun preservesProjectsContextsAndDraftScopeAfterReopen() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "round-trip-${System.nanoTime()}.db"
        try {
            Room.databaseBuilder(context, MabhasDatabase::class.java, name).build().use { db ->
                val dao = db.projects()
                dao.upsertProject(ProjectEntity("one", "پروژه یک", "1399-v4", 1, 1))
                dao.upsertProject(ProjectEntity("two", "پروژه دو", "1399-v4", 2, 2))
                dao.upsertContext(ContextEntity("one", "c181", "o01", 1000.0, 4))
                dao.upsertDraft(DraftEntity("one", "1399-v4", "air", "{}", 1))
            }
            Room.databaseBuilder(context, MabhasDatabase::class.java, name).build().use { db ->
                val dao = db.projects()
                assertEquals(2, dao.observeProjects().first().size)
                assertEquals("پروژه یک", dao.observeProject("one").first()?.title)
                assertEquals(1000.0, dao.observeContext("one").first()?.conditionedAreaM2)
                assertEquals(1L, dao.observeDraft("one", "1399-v4", "air").first()?.localRevision)
                assertNull(dao.observeDraft("two", "1399-v4", "air").first())
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
