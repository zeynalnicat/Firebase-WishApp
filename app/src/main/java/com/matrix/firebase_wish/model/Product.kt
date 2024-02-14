package com.matrix.firebase_wish.model

import java.io.Serializable

data class Product  (
    val brand: String,
    val category: String,
    val description: String,
    val id: String,
    val images: String,
    val price: Int,
    val rating: Double,
    val stock: Int ,
    val title: String,
    var isAdded: Boolean = false
) : Serializable