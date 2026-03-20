package com.example.memento
import android.icu.text.SimpleDateFormat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object DateModule {

    @Provides
    fun provideDateFormatter(): SimpleDateFormat {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }
}

//@Module
//@InstallIn(SingletonComponent::class)
//object FireBaseModule {
//
//}
