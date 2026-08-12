package com.wifisense.motiontracker.di

import android.content.Context
import androidx.room.Room
import com.wifisense.motiontracker.data.db.AppDatabase
import com.wifisense.motiontracker.data.db.SessionDao
import com.wifisense.motiontracker.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        Constants.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideSessionDao(database: AppDatabase): SessionDao =
        database.sessionDao()
}
