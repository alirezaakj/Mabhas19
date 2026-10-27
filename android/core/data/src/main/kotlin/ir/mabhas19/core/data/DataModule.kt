package ir.mabhas19.core.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.mabhas19.core.database.MabhasDatabase
import ir.mabhas19.core.domain.AppClock
import ir.mabhas19.core.domain.DraftRepository
import ir.mabhas19.core.domain.IdGenerator
import ir.mabhas19.core.domain.PreferencesRepository
import ir.mabhas19.core.domain.ProjectRepository
import ir.mabhas19.core.network.PublicApi
import ir.mabhas19.core.network.PublicApiFactory
import java.util.UUID
import javax.inject.Singleton

private val Context.settings by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context): MabhasDatabase =
        Room.databaseBuilder(context, MabhasDatabase::class.java, "mabhas19-guest.db").build()

    @Provides fun clock(): AppClock = AppClock(System::currentTimeMillis)

    @Provides fun ids(): IdGenerator = IdGenerator { UUID.randomUUID().toString() }

    @Provides fun projects(repository: LocalProjectRepository): ProjectRepository = repository

    @Provides fun drafts(repository: LocalDraftRepository): DraftRepository = repository

    @Provides fun preferences(repository: LocalPreferences): PreferencesRepository = repository

    @Provides @Singleton
    fun publicApi(): PublicApi = PublicApiFactory.create()

    @Provides @Singleton
    fun settings(@ApplicationContext context: Context) = context.settings
}
