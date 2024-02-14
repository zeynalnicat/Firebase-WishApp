package com.matrix.firebase_wish.viewmodel.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.matrix.firebase_wish.viewmodel.WishListViewModel

class WishListFactory(private val firestore: FirebaseFirestore,private val firebaseAuth: FirebaseAuth):ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WishListViewModel(firestore,firebaseAuth) as T
    }
}