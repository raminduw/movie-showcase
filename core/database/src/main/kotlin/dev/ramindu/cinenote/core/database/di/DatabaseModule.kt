package dev.ramindu.cinenote.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ramindu.cinenote.core.database.CineNoteDatabase
import dev.ramindu.cinenote.core.database.FavoriteDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CineNoteDatabase =
        Room.databaseBuilder(context, CineNoteDatabase::class.java, CineNoteDatabase.NAME).build()

    @Provides
    fun provideFavoriteDao(database: CineNoteDatabase): FavoriteDao = database.favoriteDao()
}
