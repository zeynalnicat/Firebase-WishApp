package com.matrix.firebase_wish.db.product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(productEntity: ProductEntity):Long

    @Query("SELECT * FROM Products")
    suspend fun getAll():List<ProductEntity>

    @Query("SELECT * FROM Products WHERE title LIKE '%' || :name || '%'")
    suspend fun getSearched(name: String): List<ProductEntity>

    @Query("Select count(id) from products")
    suspend fun checkDb():Int


}