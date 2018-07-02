package jajcompany.jajmeup.Utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import jajcompany.jajmeup.Models.User

object FireStore {
    private val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = fireStoreInstance.document("users/${FirebaseAuth.getInstance().uid?: throw NullPointerException("UID current user null")}")

    fun initCurrentUser(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "", "", null)
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else{
                onComplete()
            }
        }
    }

    fun updateCurrentUser(name: String = "", email: String = "", profilePicture: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name != "") userFieldMap["name"] = name
        if (email != "") userFieldMap["email"] = email
        if (profilePicture != null) userFieldMap["profilePicture"] = profilePicture
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get()
                .addOnSuccessListener {
                    onComplete(it.toObject(User::class.java)!!)
                }
    }
}