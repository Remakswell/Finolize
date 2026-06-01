package com.finolize.app.di

import android.content.Context
import androidx.room.Room
import com.finolize.app.data.local.database.FinolizeDatabase
import com.finolize.app.data.repository.ExpenseRepositoryImpl
import com.finolize.app.domain.repository.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext

import jakarta.inject.Singleton
import kotlin.jvm.java

@Module @InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinolizeDatabase {
        return Room.databaseBuilder(
            context,
            FinolizeDatabase::class.java,
            "finolize_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideRepository(db: FinolizeDatabase): ExpenseRepository {
        return ExpenseRepositoryImpl(db.expenseDao())
    }
}