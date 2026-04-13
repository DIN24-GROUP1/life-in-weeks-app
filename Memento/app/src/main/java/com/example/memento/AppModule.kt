package com.example.memento

import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.room.Room
import com.example.memento.db.AppDatabase
import com.example.memento.db.LifePhaseDao
import com.example.memento.db.WeekTagDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object DateModule {

    @Provides
    fun provideDateFormatter(): SimpleDateFormat {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "memento.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePhaseDao(db: AppDatabase): LifePhaseDao = db.phaseDao()

    @Provides
    fun provideWeekTagDao(db: AppDatabase): WeekTagDao = db.weekTagDao()
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
