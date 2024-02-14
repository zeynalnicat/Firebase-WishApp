package com.matrix.firebase_wish.di

import android.content.Context
import androidx.room.Room
import com.matrix.firebase_wish.db.RoomDb
import com.matrix.firebase_wish.db.product.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun roomDatabase(@ApplicationContext context: Context): RoomDb {
        return Room.databaseBuilder(
            context,
            RoomDb::class.java,
            "WishList"
        ).build()
    }

    @Singleton
    @Provides
    fun provideProductDao(roomDb: RoomDb):ProductDao{
        return roomDb.productDao()
    }


}