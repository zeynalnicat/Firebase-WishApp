package com.matrix.firebase_wish.viewmodel.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.matrix.firebase_wish.db.product.ProductDao
import com.matrix.firebase_wish.viewmodel.HomeViewModel

class HomeFactory(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val productDao: ProductDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(firestore, firebaseAuth, productDao) as T
    }
}