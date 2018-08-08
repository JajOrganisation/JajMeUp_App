package jajcompany.jajmeup.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.xwray.groupie.kotlinandroidextensions.Item
import jajcompany.jajmeup.Activity.YouTubeJAJActivity
import jajcompany.jajmeup.Models.User
import jajcompany.jajmeup.Models.Vote
import jajcompany.jajmeup.R
import jajcompany.jajmeup.RecycleView.item.VoteItem
import jajcompany.jajmeup.RecycleView.item.UserItem

object FireStore {
    private val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

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

    fun updateCurrentUser(name: String = "", reveil: String = "", profilePicture: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name != "") userFieldMap["name"] = name
        if (reveil != "") userFieldMap["reveil"] = reveil
        if (profilePicture != null) userFieldMap["profilePicture"] = profilePicture
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get()
                .addOnSuccessListener {
                    onComplete(it.toObject(User::class.java)!!)
                }
    }

    fun getLastReveil(context: Context) {
        fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/reveilVote")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            var test = document.toObject(Vote::class.java)
                            val intent = Intent()
                            Log.d("HELLO", test.votant)
                            intent.action = "onReveilINFO"
                            intent.putExtra("lien", test.lien)
                            intent.putExtra("votant", test.votant)
                            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                            context.sendBroadcast(intent)
                        }
                    } else {
                        Log.e("FIRESTORE", "Users listener error.")
                    }
                }
    }

    fun addUsersListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                            items.add(UserItem(it.toObject(User::class.java)!!, it.id, context))
                    }
                    onListen(items)
                }
    }

    fun addReveilListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/reveilVote")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Reveil listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                            items.add(VoteItem(it.toObject(Vote::class.java)!!, it.id, context))
                    }
                    onListen(items)
                }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun sendVote(vote: Vote, otherUserId: String) {
        fireStoreInstance.document("users/${otherUserId}")
                .collection("reveilVote")
                .add(vote)
    }
}