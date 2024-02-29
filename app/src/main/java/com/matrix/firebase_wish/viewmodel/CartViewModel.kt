package com.matrix.firebase_wish.viewmodel

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

class CartViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _products = MutableLiveData<Resource<List<Product>>>()
    val products: LiveData<Resource<List<Product>>>
        get() = _products


    fun readData() {
        _products.postValue(Resource.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ids = getIds()
                val productList = mutableListOf<Product>()
                ids.forEach {
                    val product = fetchProduct(it)
                    productList.add(product)

                }
                _products.postValue(Resource.Success(productList))

            } catch (e: Exception) {
                _products.postValue(Resource.Error(e))
            }

        }
    }


    suspend fun fetchProduct(id: String): Product {
        val productRef = firestore.collection("products")
        val query = productRef.whereEqualTo("id", id).get().await()
        val document = query.documents[0]

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
            isCart = true
        )

        return product

    }
    suspend fun getIds(): List<String> {
        val userId = firebaseAuth.currentUser?.uid
        val cartRef = firestore.collection("cart")
        val query = cartRef.whereEqualTo("userId", userId).get().await()
        val listIds = mutableListOf<String>()

        for (document in query.documents) {
            val id = document.getString("productId") ?: ""
            listIds.add(id)
        }
        return listIds
    }

}