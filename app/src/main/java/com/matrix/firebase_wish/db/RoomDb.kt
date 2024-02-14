package com.matrix.firebase_wish.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.matrix.firebase_wish.db.product.ProductDao
import com.matrix.firebase_wish.db.product.ProductEntity



@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class RoomDb : RoomDatabase() {
    abstract fun productDao():ProductDao


}