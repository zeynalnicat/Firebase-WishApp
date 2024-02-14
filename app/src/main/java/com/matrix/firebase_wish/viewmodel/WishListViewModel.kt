package com.matrix.firebase_wish.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.matrix.firebase_wish.model.Product


class WishListViewModel(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    private val _products = MutableLiveData<List<Product>>()

    val products: LiveData<List<Product>>
        get() = _products

    fun getProducts() {
        val listIds = mutableListOf<String>()
        val listProducts = mutableListOf<Product>()
        val userId = firebaseAuth.currentUser?.uid
        val wishListRef = firestore.collection("wishlist").whereEqualTo("userId", userId)
        val productListRef = firestore.collection("products")

        try {
            wishListRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val data = querySnapshot.documents
                    data.forEach { document ->
                        val item = document.data
                        val id = item?.get("productId") as? String
                        id?.let { listIds.add(it) }
                    }

                    if (listIds.isNotEmpty()) {
                        val tasks = mutableListOf<Task<QuerySnapshot>>()

                        listIds.forEach { id ->
                            val query = productListRef.whereEqualTo("id", id)
                            val task = query.get()
                                .addOnSuccessListener { querySnapshot ->
                                    val documents = querySnapshot.documents
                                    if (documents.isNotEmpty()) {
                                        val data = documents[0].data
                                        data?.let { productModel ->
                                            val product = Product(
                                                data["brand"].toString(),
                                                data["category"].toString(),
                                                data["description"].toString(),
                                                data["id"].toString(),
                                                data["imgUri"].toString(),
                                                data["price"].toString().toInt(),
                                                data["rating"].toString().toDouble(),
                                                data["stocks"].toString().toInt(),
                                                data["title"].toString(),
                                                isAdded = false
                                            )
                                            listProducts.add(product)
                                        }
                                    }
                                }
                            tasks.add(task)
                        }

                        Tasks.whenAllSuccess<QuerySnapshot>(tasks)
                            .addOnSuccessListener {
                                _products.postValue(listProducts)
                            }
                            .addOnFailureListener { exception ->
                                Log.e(
                                    "Firestore",
                                    "Error retrieving products: ${exception.message}"
                                )
                            }
                    } else {
                        _products.postValue(listProducts)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error retrieving wishlist: ${exception.message}")
                }
        } catch (e: Exception) {
            Log.e("wishList", e.message ?: "")
        }
    }
}