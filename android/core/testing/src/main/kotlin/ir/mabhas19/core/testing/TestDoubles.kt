package ir.mabhas19.core.testing

import ir.mabhas19.core.domain.ProjectRepository
import ir.mabhas19.core.model.Project
import ir.mabhas19.core.model.ProjectContext
import ir.mabhas19.core.model.ProjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class FakeProjectRepository : ProjectRepository {
    val projects = MutableStateFlow<List<Project>>(emptyList())
    private val contexts = MutableStateFlow<Map<ProjectId, ProjectContext>>(emptyMap())
    var failWrites = false

    override fun observeProjects() = projects
    override fun observeProject(id: ProjectId) = projects.map { list -> list.find { it.id == id } }
    override fun observeContext(id: ProjectId) = contexts.map { it[id] }

    override suspend fun create(title: String): ProjectId {
        check(!failWrites) { "Disk unavailable" }
        val id = ProjectId("test-${projects.value.size + 1}")
        projects.value += Project(id, title.trim(), createdAtMillis = 0, updatedAtMillis = 0)
        return id
    }

    override suspend fun saveContext(context: ProjectContext) {
        contexts.value += context.projectId to context
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    val dispatcher = StandardTestDispatcher()
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
