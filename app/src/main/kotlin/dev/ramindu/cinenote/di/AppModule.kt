package dev.ramindu.cinenote.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ramindu.cinenote.BuildConfig
import dev.ramindu.cinenote.core.network.TmdbToken
import java.time.Clock

/** App-wide values only the composition root knows about. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @TmdbToken
    fun provideTmdbToken(): String = BuildConfig.TMDB_TOKEN

    /** Injected so use cases stay deterministic in tests (Clock.fixed). */
    @Provides
    fun provideClock(): Clock = Clock.systemUTC()
}
