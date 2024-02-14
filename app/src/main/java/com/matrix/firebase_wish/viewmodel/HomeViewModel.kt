package com.matrix.firebase_wish.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.matrix.firebase_wish.db.product.ProductDao
import com.matrix.firebase_wish.db.product.ProductEntity
import com.matrix.firebase_wish.model.Product

import com.matrix.firebase_wish.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


class HomeViewModel(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val productDao: ProductDao,
) :
    ViewModel() {
    private val _products = MutableLiveData<Resource<List<Product>>>()

    private val _roomSize = MutableLiveData<Int>(0)
    val products: LiveData<Resource<List<Product>>>
        get() = _products


    private var productHolders: List<Product> = emptyList()
    private val _categories =
        MutableLiveData<Resource<Set<String>>>(Resource.Success(hashSetOf()))
    val categories: LiveData<Resource<Set<String>>>
        get() = _categories

    val roomSize: LiveData<Int>
        get() = _roomSize

    val isRoom = MutableLiveData(false)

    fun readData() {
        viewModelScope.launch(Dispatchers.IO) {
            _products.postValue(Resource.Loading)
            try {
                val productList = fetchProductList()
                val wishlistMap = getWishlistMap()


                productList.forEach { product ->
                    product.isAdded = wishlistMap[product.id] == true

                }

                _products.postValue(Resource.Success(productList))
                productHolders = productList
                isRoom.postValue(false)

            } catch (e: Exception) {
                _products.postValue(Resource.Error(e))
                getFromDB()
            }
        }
    }

    private suspend fun fetchProductList(): List<Product> {
        val querySnapshot = firestore.collection("products").get().await()
        return querySnapshot.documents.map { document ->
            Product(
                document["brand"].toString(),
                document["category"].toString(),
                document["description"].toString(),
                document["id"].toString(),
                document["imgUri"].toString(),
                document["price"].toString().toInt(),
                document["rating"].toString().toDouble(),
                document["stocks"].toString().toInt(),
                document["title"].toString(),
            )

        }
    }

    private suspend fun getWishlistMap(): Map<String, Boolean> {
        val wishRef = firestore.collection("wishlist")
        val userId = firebaseAuth.currentUser?.uid
        val wishlistSnapshot = wishRef.whereEqualTo("userId",userId).get().await()
        val wishlistMap = mutableMapOf<String, Boolean>()
        for (document in wishlistSnapshot.documents) {
            val productId = document.getString("productId") ?: ""
            wishlistMap[productId] = true
        }
        return wishlistMap
    }


    fun search(query: String) {
        val productList = _products.value
        if (productList is Resource.Success) {
            if (query.isEmpty()) {
                readData()
            } else {
                val newList =
                    productList.data.filter { it.title.contains(query, ignoreCase = true) }

                if (newList.isNotEmpty()) {
                    _products.postValue(Resource.Success(newList))
                }

            }
        }
    }

    fun insertDB() {
        viewModelScope.launch(Dispatchers.IO) {
            val productList = _products.value
            if (productList is Resource.Success) {
                productList.data.forEach {
                    productDao.insert(
                        ProductEntity(
                            id = it.id,
                            brand = it.brand,
                            description = it.description,
                            title = it.title,
                            price = it.price,
                            stock = it.stock,
                            images = it.images,
                            category = it.category,
                            rating = it.rating
                        )
                    )
                }
            }
        }
    }

    fun getCategories() {
        val currentCategories = (_categories.value as? Resource.Success)?.data ?: emptySet()
        if (currentCategories.isNotEmpty()) {
            return
        }

        val listCategories = mutableSetOf("All")
        val productList = _products.value
        if (productList is Resource.Success) {
            val data = productList.data
            data.forEach {
                listCategories.add(it.category)
            }
            _categories.postValue(Resource.Success(listCategories))
        }
    }

    fun insertWishList(productId: String) {
        val wishRef = firestore.collection("wishlist")
        val userId = firebaseAuth.currentUser?.uid

        val doesExistQuery = wishRef
            .whereEqualTo("productId", productId)
            .whereEqualTo("userId", userId)

        doesExistQuery.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val existingDocuments = task.result
                if (existingDocuments != null && !existingDocuments.isEmpty) {
                    val wishRef = firestore.collection("wishlist")

                    val documentId = existingDocuments.documents[0].id
                    wishRef.document(documentId).delete()


                } else {

                    val uuid = UUID.randomUUID().toString()
                    val hashMap = hashMapOf(
                        "id" to uuid,
                        "productId" to productId,
                        "userId" to userId
                    )
                    wishRef.add(hashMap)
                }
            }
        }
    }


    fun getFromDB() {
        _products.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val products = productDao.getAll()
            if (products.isNotEmpty()) {
                val productList = products.map {
                    Product(
                        id = it.id,
                        brand = it.brand,
                        description = it.description,
                        title = it.title,
                        price = it.price,
                        stock = it.stock,
                        images = it.images,
                        category = it.category,
                        rating = it.rating
                    )
                }
                _products.postValue(Resource.Success(productList))
                _roomSize.postValue(products.size)
                isRoom.postValue(true)
            }
        }
    }

    fun checkDb() {
        viewModelScope.launch {
            val size = productDao.checkDb()
            _roomSize.postValue(size)
        }
    }


    fun getDbDueCategories(category: String) {
        val productList = productHolders
        if (category == "All") {
            readData()
        } else {
            val filteredList = productList.filter { it.category == category }
            _products.postValue(Resource.Success(filteredList.toList()))
        }


    }


    fun searchRoom(string: String) {
        if (string.isEmpty()) {
            getFromDB()
        } else {
            try {
                _products.postValue(Resource.Loading)
                viewModelScope.launch(Dispatchers.IO) {
                    val productList = productDao.getSearched(string)
                    if (productList.isNotEmpty()) {
                        val productRoomModel = productList.map {
                            Product(
                                id = it.id,
                                brand = it.brand,
                                description = it.description,
                                title = it.title,
                                price = it.price,
                                stock = it.stock,
                                images = it.images,
                                category = it.category,
                                rating = it.rating
                            )
                        }
                        _products.postValue(Resource.Success(productRoomModel))
                    }
                }
            } catch (e: Exception) {
                _products.postValue(Resource.Error(e))

            }
        }
    }

    fun sort(sorting: String, isRoom: Boolean) {
        val productList = _products.value
        if (productList is Resource.Success) {

            when (sorting) {
                "Default" -> {
                    if (!isRoom) {
                        readData()
                    } else {
                        getFromDB()
                    }
                }

                "A-Z" -> _products.postValue(Resource.Success(productList.data.sortedBy { it.title }))
                "Z-A" -> _products.postValue(Resource.Success(productList.data.sortedByDescending { it.title }))
                "ASC-Price" -> _products.postValue(Resource.Success(productList.data.sortedBy { it.price }))
                "DESC-Price" -> _products.postValue(Resource.Success(productList.data.sortedByDescending { it.price }))
            }
        }
    }


}