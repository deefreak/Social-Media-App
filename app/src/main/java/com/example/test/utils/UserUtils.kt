package com.example.test.utils

import com.example.test.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


object UserUtils {
    var user: User? = null

    fun getCurrentUser(){
        if(FirebaseAuth.getInstance().currentUser != null){
            FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                .get().addOnCompleteListener {
                    user = it.result?.toObject(User::class.java)
                }
        }
    }
}