package com.matrix.firebase_wish.db.product

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("Products")
data class ProductEntity (
    @PrimaryKey
    val id :String ,
    val brand: String,
    val category: String,
    val description: String,
    val images: String,
    val price: Int,
    val rating: Double,
    val stock: Int,
    val title: String
)