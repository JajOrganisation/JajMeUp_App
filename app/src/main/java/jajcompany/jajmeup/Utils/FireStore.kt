package jajcompany.jajmeup.Utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.xwray.groupie.kotlinandroidextensions.Item
import jajcompany.jajmeup.Models.AskingFriends
import jajcompany.jajmeup.Models.User
import jajcompany.jajmeup.Models.Vote
import jajcompany.jajmeup.RecycleView.item.AskingFriendsItem
import jajcompany.jajmeup.RecycleView.item.VoteItem
import jajcompany.jajmeup.RecycleView.item.UserItem


object FireStore {

    private var FriendsList: MutableList<User> = arrayListOf()

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
                            intent.putExtra("message", test.message)
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
                            if ((!FriendsList.contains(it.toObject(User::class.java)!!)) || (FriendsList.isEmpty()))
                                items.add(UserItem(it.toObject(User::class.java)!!, it.id, context))
                    }
                    onListen(items)
                }
    }

    fun addFriendsListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/friends")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                            FriendsList.add(it.toObject(User::class.java)!!)
                            items.add(UserItem(it.toObject(User::class.java)!!, it.id, context))
                    }
                    onListen(items)
                }
    }

    fun searchUser(context: Context, onListen: (List<Item>) -> Unit, txtSearch: String): ListenerRegistration {
        return fireStoreInstance.collection("users")
                .orderBy("name")
                .startAt(txtSearch)
                .limit(20)
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

    fun addAskingFriendsListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/askFriends")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Ask Friends listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                            items.add(AskingFriendsItem(it.toObject(AskingFriends::class.java)!!, it.id, context))
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

    fun askFriends(userAsk: AskingFriends, otherUserID: String) {
        fireStoreInstance.document("users/${otherUserID}")
                .collection("askFriends")
                .add(userAsk)
    }

    fun addFriends(userAsk: AskingFriends) {
        fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
                .collection("friends")
                .add(userAsk)
        val docRef = fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
                .collection("askFriends")
                .whereEqualTo("name", userAsk.name)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        task.result.forEach {
                            Log.d("LOGGER", "COUCOU on a des resultats")
                            it.reference.delete()
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.exception)
                    }
                }
        var myprofil = AskingFriends()
        val myuser = FireStore.getCurrentUser { myuser ->
            if (myuser.profilePicture != null) {
                val user = FirebaseAuth.getInstance()
                val profilepath = myuser.profilePicture
                myprofil = AskingFriends(user!!.uid.toString(), myuser.name, profilepath)

            }
            fireStoreInstance.document("users/${userAsk.uid}")
                    .collection("friends")
                    .add(myprofil)
        }
    }

    fun getProfilePicture(): String {
        val auth = FirebaseAuth.getInstance()
        val usercurrent = auth.currentUser
        var result = ""
        val docRef = fireStoreInstance.document("users/${usercurrent!!.uid}")
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                result = document.getString("profilePicture").toString()
            } else {
                Log.d("LOGGER", "get failed with ", task.exception)
            }
        }
        Log.d("COUCOUu", result)
        return result
    }

    fun isFriend(context: Context, nameFriends: UserItem, onListen: (UserItem) -> Unit): ListenerRegistration {
        return fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
                .collection("friends")
                .whereEqualTo("name", nameFriends.user.name)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot!!.documents.size > 0)
                        onListen(nameFriends)
                    else {
                        val nop = UserItem(User(), nameFriends.userId, context)
                        onListen(nop)
                    }
                }
    }

    fun acceptFriends(newFriend: User) {
        val user = FirebaseAuth.getInstance().currentUser
        fireStoreInstance.collection("askFriends")
                .document("user/${user!!.uid}")
                .delete()
        fireStoreInstance.document("users/${newFriend}")
                .collection("friends")
                .add(user!!.uid)
        fireStoreInstance.document("users/${user!!.uid}")
                .collection("friends")
                .add(newFriend)
    }
}