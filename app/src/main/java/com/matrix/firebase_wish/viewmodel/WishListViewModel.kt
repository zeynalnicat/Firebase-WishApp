package com.matrix.firebase_wish.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.matrix.firebase_wish.model.Product
import com.matrix.firebase_wish.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


class WishListViewModel(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    private val _products = MutableLiveData<Resource<List<Product>>>()

    val products: LiveData<Resource<List<Product>>>
        get() = _products

    fun getProducts() {
        _products.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val ids = getIds()
            val datas = fetchData(ids)
            val cartMap = getCartMap()

            datas.forEach {
                it.isAddedCart = cartMap.contains(it.id)
            }
            _products.postValue(Resource.Success(datas))
        }

        try {


        } catch (e: Exception) {
            _products.postValue(Resource.Error(e))
        }
    }

    private suspend fun getIds(): List<String> {
        val listIds = mutableListOf<String>()
        val userId = firebaseAuth.currentUser?.uid
        val wishListRef =
            firestore.collection("wishlist").whereEqualTo("userId", userId).get().await()

        for (document in wishListRef.documents) {
            val id = document.getString("productId")
            listIds.add(id ?: "")
        }

        return listIds

    }

    private suspend fun fetchData(list: List<String>): List<Product> {
        val productListRef = firestore.collection("products")
        val listProducts = mutableListOf<Product>()
        list.forEach { id ->
            val query = productListRef.whereEqualTo("id", id).get().await()

            for (document in query.documents) {
                val product = Product(
                    document["brand"].toString(),
                    document["category"].toString(),
                    document["description"].toString(),
                    document["id"].toString(),
                    document["imgUri"].toString(),
                    document["price"].toString().toInt(),
                    document["rating"].toString().toDouble(),
                    document["stocks"].toString().toInt(),
                    document["title"].toString(),
                    isAdded = false
                )
                listProducts.add(product)
            }

        }
        return listProducts
    }


    private suspend fun getCartMap(): List<String> {
        val cartRef = firestore.collection("cart")
        val userId = firebaseAuth.currentUser?.uid
        val cartSnapshot = cartRef.whereEqualTo("userId", userId).get().await()
        val cartMap = mutableListOf<String>()
        for (document in cartSnapshot.documents) {
            val productId = document.getString("productId") ?: ""
            cartMap.add(productId)
        }
        return cartMap
    }

    fun insertCart(productId: String) {
        val userId = firebaseAuth.currentUser?.uid
        val cartRef = firestore.collection("cart")

        val doesExistQuery =
            cartRef.whereEqualTo("userId", userId).whereEqualTo("productId", productId)
        doesExistQuery.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val existingDocuments = task.result
                if (existingDocuments != null && !existingDocuments.isEmpty) {
                    val documentId = existingDocuments.documents[0].id
                    cartRef.document(documentId).delete()
                } else {
                    val uuid = UUID.randomUUID().toString()
                    val hashMap = hashMapOf(
                        "id" to uuid,
                        "productId" to productId,
                        "userId" to userId
                    )
                    cartRef.add(hashMap)
                }
            }
        }

    }
}