package dev.ramindu.cinenote.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ramindu.cinenote.core.data.repository.OfflineFavoritesRepository
import dev.ramindu.cinenote.core.data.repository.TmdbMovieRepository
import dev.ramindu.cinenote.core.domain.repository.FavoritesReader
import dev.ramindu.cinenote.core.domain.repository.FavoritesRepository
import dev.ramindu.cinenote.core.domain.repository.FavoritesWriter
import dev.ramindu.cinenote.core.domain.repository.MovieRepository

/** Wires domain contracts to their data-layer implementations (Dependency Inversion in practice). */
@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindMovieRepository(impl: TmdbMovieRepository): MovieRepository

    @Binds
    fun bindFavoritesRepository(impl: OfflineFavoritesRepository): FavoritesRepository

    @Binds
    fun bindFavoritesReader(impl: OfflineFavoritesRepository): FavoritesReader

    @Binds
    fun bindFavoritesWriter(impl: OfflineFavoritesRepository): FavoritesWriter
}
