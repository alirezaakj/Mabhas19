package ir.mabhas19.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ir.mabhas19.core.database.MabhasDatabase
import ir.mabhas19.core.domain.AppClock
import ir.mabhas19.core.domain.DraftConflictException
import ir.mabhas19.core.domain.IdGenerator
import ir.mabhas19.core.model.SectionDraft
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalRepositoriesTest {
    @Test fun rejectsStaleLocalDraftWithoutOverwritingSavedWork() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Room.inMemoryDatabaseBuilder(context, MabhasDatabase::class.java).build().use { db ->
            val projects =
                LocalProjectRepository(db, AppClock { 1234L }, IdGenerator { "local-one" })
            val id = projects.create("  پرونده  ")
            assertEquals("پرونده", projects.observeProject(id).first()?.title)
            val repository = LocalDraftRepository(db)
            val first = repository.saveDraft(SectionDraft(id, "air", "{\"value\":1}"))
            assertEquals(1L, first.localRevision)
            try {
                repository.saveDraft(SectionDraft(id, "air", "{\"value\":2}"))
                fail("Stale revision should fail")
            } catch (_: DraftConflictException) {
                assertEquals(first, repository.observeDraft(id, "air").first())
            }
        }
    }
}
