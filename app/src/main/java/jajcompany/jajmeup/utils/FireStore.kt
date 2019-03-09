package jajcompany.jajmeup.utils

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.xwray.groupie.kotlinandroidextensions.Item
import jajcompany.jajmeup.RecycleView.item.AskingFriendsItem
import jajcompany.jajmeup.RecycleView.item.NotifItem
import jajcompany.jajmeup.RecycleView.item.UserItem
import jajcompany.jajmeup.RecycleView.item.VoteItem
import jajcompany.jajmeup.models.*
import java.util.*


object FireStore {

    private val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    fun initCurrentUser(myname: String, myprofilepicture: String, onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = UserRegister(FirebaseAuth.getInstance().currentUser!!.uid, myname, "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "down", myprofilepicture)
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    fireStoreInstance.document("counters/count")
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    var test = task.result!!.toObject(Counters::class.java)
                                    val countersFieldMap = mutableMapOf<String, Any>()
                                    countersFieldMap["users_count"] = test!!.users_count + 1
                                    Log.d("HELLO", "My number "+test.users_count)
                                    fireStoreInstance.document("counters/count/")
                                            .update(countersFieldMap)
                                            .addOnCompleteListener {
                                                updateCurrentUser(mynumber = test.users_count + 1)
                                                onComplete()
                                            }
                                            .addOnFailureListener { e -> Log.d("HELLO", "Error set number", e) }
                                }
                            }
                            .addOnFailureListener { e -> Log.d("HELLO", "Error ici set number", e) }
                }
            }
            else{
                onComplete()
            }
        }
    }

    fun updateCurrentUser(name: String = "", reveilDefault: String = "", reveilCurrent: String = "", mynumber: Int = 0, authorization: Int = -1, profilePicture: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name != "") userFieldMap["name"] = name
        if (reveilDefault != "") userFieldMap["reveilDefaultLink"] = reveilDefault
        if (reveilCurrent != "") userFieldMap["reveilCurrentHour"] = reveilCurrent
        if(mynumber != 0) userFieldMap["mynumber"] = mynumber
        if (profilePicture != null) userFieldMap["profilePicture"] = profilePicture
        if (authorization != -1) userFieldMap["authorization"] = authorization
        currentUserDocRef.update(userFieldMap)
                .addOnFailureListener {
                    Log.d("HELLO", "Error update "+it)
                }
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get()
                .addOnSuccessListener {
                    onComplete(it.toObject(User::class.java)!!)
                }
    }

    fun getLastReveil(context: Context) {
        var flag = false
        fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/reveilVote")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            flag = true
                            val tmpvote = document.toObject(Vote::class.java)
                            fireStoreInstance.document("users/${tmpvote.votant}")
                                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                        if (task.isSuccessful) {
                                            val test = querySnapshot!!.get("name").toString()
                                            Log.d("HELLO", "COUCOU"+test)
                                            val intent = Intent()
                                            intent.action = "onReveilINFO"
                                            intent.putExtra("lien", tmpvote.lien)
                                            intent.putExtra("votant", test)
                                            intent.putExtra("votantuid", tmpvote.votant)
                                            intent.putExtra("message", tmpvote.message)
                                            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                                            context.sendBroadcast(intent)
                                        }
                                        else {
                                            Log.e("FIRESTORE", "Get User Alarm error.")
                                        }
                            }
                        }
                        if(!flag) {
                            val intent = Intent()
                            intent.action = "onReveilINFO"
                            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                            intent.putExtra("lien", sharedPreferences.getString("default_reveil", "dQw4w9WgXcQ"))
                            intent.putExtra("votant", "Ton réveil")
                            intent.putExtra("message", "Tu n'as pas reçu de vote")
                            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                            context.sendBroadcast(intent)
                        }
                    } else {
                        Log.e("FIRESTORE", "Reveil last listener error.")
                    }
                }
    }

    fun setRandomUserNumber(context: Context) {
        fireStoreInstance.document("counters/count/")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Error setRandomUser", firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    var totaluser = querySnapshot!!.get("users_count").toString().toInt()
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val editor = sharedPreferences.edit()
                    var therand = Random().nextInt((totaluser))+1
                    if (therand+20 > totaluser) {
                        therand = totaluser - 20
                    }
                    editor.putInt("randomUser", therand)
                    editor.apply()
                }
    }


    fun getUsers(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return fireStoreInstance.document("counters/count/")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    var totaluser = querySnapshot!!.get("users_count").toString().toInt()
                    if (totaluser > 20) {
                        fireStoreInstance.collection("users")
                                .whereGreaterThanOrEqualTo("mynumber", PreferenceManager.getDefaultSharedPreferences(context).getInt("randomUser", 1))
                                .whereEqualTo("authorization", 2)
                                .limit(20)
                                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                    if (firebaseFirestoreException != null) {
                                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                                        return@addSnapshotListener
                                    }
                                    val items = mutableListOf<Item>()
                                    querySnapshot!!.documents.forEach {
                                        val saveit = it
                                        fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                                                ?: throw NullPointerException("UID is null.")}")
                                                .collection("friends")
                                                .whereEqualTo("uid", saveit["uid"])
                                                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                                    if (querySnapshot!!.documents.size == 0) {
                                                        items.add(UserItem(it.toObject(User::class.java)!!, it.id, context))
                                                        onListen(items)
                                                    }
                                                }
                                        onListen(items)
                                    }
                                }
                    } else {
                        fireStoreInstance.collection("users")
                                .whereGreaterThanOrEqualTo("mynumber", 1)
                                .whereEqualTo("authorization", 2)
                                .addSnapshotListener { queryUsers, firebaseFirestoreExceptionUsers ->
                                    if (firebaseFirestoreExceptionUsers != null) {
                                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreExceptionUsers)
                                        return@addSnapshotListener
                                    }
                                    val items = mutableListOf<Item>()
                                    queryUsers!!.documents.forEach {
                                        val saveit = it
                                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid) {
                                            fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                                                    ?: throw NullPointerException("UID is null.")}")
                                                    .collection("friends")
                                                    .whereEqualTo("uid", saveit["uid"])
                                                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                                        if (firebaseFirestoreException != null) {
                                                            Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                                                            return@addSnapshotListener
                                                        }
                                                        if (querySnapshot!!.documents.size == 0) {
                                                            items.add(UserItem(it.toObject(User::class.java)!!, it.id, context))
                                                            onListen(items)
                                                        }
                                                    }
                                        }
                                       // onListen(items)
                                    }
                                }
                    }
                }
    }

    fun newFriendsListener(onListen: (List<String>) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/friends")
                .addSnapshotListener { queryFriends, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Friends listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    if (queryFriends!!.size() != 0) {
                        val items = mutableListOf<String>()
                        queryFriends.documents.forEach { currentDoc ->
                            items.add(currentDoc["uid"].toString())
                            onListen(items)
                        }
                    }
                    else {
                        val items = mutableListOf<String>()
                        items.add(0, "Nothing")
                        onListen(items)
                    }
                }
    }

    fun getAllFriendUID(context: Context) {
        fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/friends")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val items = ArrayList<String>()
                        task.result!!.forEach { friend ->
                            items.add(friend["uid"].toString())
                        }
                        Log.d("HELLO", "on envoi"+items)
                        val intent = Intent()
                        intent.action = "onAllFriends"
                        intent.putExtra("uidList", items)
                        context.sendBroadcast(intent)
                    } else {
                        Log.d("LOGGER", "get failed with ", task.exception)
                    }
                }
    }

    fun addFriendsListener(context: Context, friendUid: String, onListen: (List<Item>, String) -> Unit): ListenerRegistration {
        Log.d("HELLO", "on va y aller"+friendUid)
        return  fireStoreInstance.collection("/users/")
                .whereEqualTo("uid", friendUid)
                .whereGreaterThanOrEqualTo("authorization", 1)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    if (querySnapshot!!.size() != 0) {
                        val items = mutableListOf<Item>()
                        querySnapshot!!.forEach {
                            //if (it["uid"] in listUidFriends){
                                Log.d("HELLO", " ici "+it)
                                items.add(UserItem(it.toObject(User::class.java)!!, it.id, context))
                                val intent = Intent()
                                intent.action = "onNewFriend"
                                context.sendBroadcast(intent)
                                onListen(items, it["uid"].toString())
                            //}
                        }
                    }
                    else {
                        Log.d("HELLO", " aie ")
                        val items = mutableListOf<Item>()
                        val intent = Intent()
                        intent.action = "onNewFriend"
                        context.sendBroadcast(intent)
                        onListen(items, "nop")
                    }
                }
    }

    fun searchUser(context: Context, onListen: (List<Item>) -> Unit, txtSearch: String): ListenerRegistration {
        return fireStoreInstance.collection("users")
                .orderBy("name")
                .startAt(txtSearch)
                .limit(20)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Search users listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid) {
                            fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                                    ?: throw NullPointerException("UID is null.")}/friends")
                                    .whereEqualTo("uid", it.id)
                                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                        if (firebaseFirestoreException != null) {
                                            Log.e("FIRESTORE", "Count Notifications listener error.", firebaseFirestoreException)
                                            return@addSnapshotListener
                                        }
                                        val isFriend = querySnapshot!!.size()
                                        Log.d("HELLO", "isFriend"+isFriend.toString())
                                        if (isFriend > 0) {
                                            var tmpClass = it.toObject(User::class.java)
                                            var newClass = User(tmpClass!!.uid, tmpClass.name, tmpClass.reveilDefaultLink, tmpClass.reveilCurrentHour, tmpClass.profilePicture, isFriend = true)
                                            items.add(UserItem(newClass, it.id, context))
                                        }
                                        else {
                                            items.add(UserItem(it.toObject(User::class.java)!!, it.id, context))
                                        }
                                        onListen(items)
                                    }
                        }
                    }
                }
    }

    fun addReveilListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/reveilVote")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Reveil listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    var flag = false
                    querySnapshot!!.documents.forEach {
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid) {
                            if (flag) {
                                val previousresult = it.toObject(Vote::class.java)
                                fireStoreInstance.collection("users/")
                                        .whereEqualTo("uid", previousresult!!.votant)
                                        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                            if (firebaseFirestoreException != null) {
                                                Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                                                return@addSnapshotListener
                                            }
                                            querySnapshot!!.documents.forEach {
                                                Log.d("HELLO", "OUI")
                                                items.add(VoteItem(previousresult, it.toObject(User::class.java)!!, context))
                                            }
                                            onListen(items)
                                        }
                            }
                            else {
                                flag = true
                            }
                        }
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
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid) {
                            fireStoreInstance.collection("users/")
                                    .whereEqualTo("uid", it["uid"].toString())
                                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                        if (firebaseFirestoreException != null) {
                                            Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                                            return@addSnapshotListener
                                        }
                                        querySnapshot!!.documents.forEach {toadd ->
                                            items.add(AskingFriendsItem(toadd.toObject(AskingFriends::class.java)!!, toadd.id, context))
                                        }
                                        onListen(items)
                                    }
                        }
                    }
                }
    }

    fun addNotificationListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/notifications")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Notification listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        var notiftmp = it.toObject(NotifWakeUp::class.java)!!
                        it.reference.update("status", "read")
                        fireStoreInstance.collection("users/")
                                .whereEqualTo("uid", it["whowokeup"].toString())
                                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                    if (firebaseFirestoreException != null) {
                                        Log.e("FIRESTORE", "Notification listener error.", firebaseFirestoreException)
                                        return@addSnapshotListener
                                    }
                                    querySnapshot!!.documents.forEach {
                                        val usertmp = it.toObject(User::class.java)!!
                                        items.add(NotifItem(notiftmp, usertmp.name, usertmp.profilePicture!!, context))
                                    }
                                    onListen(items)
                                }
                    }
                    onListen(items)
                }
    }

    fun addRemovedFriendsListener(onListen: (List<String>) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/removedFriends")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Ask Friends listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<String>()
                    querySnapshot!!.documents.forEach {
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid) {
                            items.add(it["uid"].toString())
                        }
                    }
                    onListen(items)
                }
    }

    fun askingFriendCount(onListen: (Int) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/askFriends")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Count Friends listener error.", firebaseFirestoreException)
                        onListen(0)
                        return@addSnapshotListener
                    }
                        val totalasking = querySnapshot!!.size()
                        onListen(totalasking)
                    }
    }

    fun notificationsCount(onListen: (Int) -> Unit): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/notifications")
                .whereEqualTo("status", "unread")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Count Notifications listener error.", firebaseFirestoreException)
                        onListen(0)
                        return@addSnapshotListener
                    }
                    val totalnotification = querySnapshot!!.size()
                    onListen(totalnotification)
                }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun sendVote(vote: Vote, otherUserId: String) {
        fireStoreInstance.document("users/${otherUserId}")
                .collection("reveilVote")
                .add(vote)
                .addOnFailureListener { e -> Log.d("HELLO", "Error send vote", e) }
    }

    fun sendNotifWakeUp(notif: NotifWakeUp, otherUserId: String) {
        fireStoreInstance.document("users/${otherUserId}")
                .collection("notifications")
                .add(notif)
                .addOnFailureListener { e -> Log.d("HELLO", "Error send notif", e) }
    }

    fun askFriends(userAsk: String, otherUserID: String) {
        val tmp: Map<String, String> = hashMapOf("uid" to userAsk)
        fireStoreInstance.document("users/${otherUserID}/")
                .collection("askFriends")
                .document(userAsk)
                .set(tmp)
                .addOnFailureListener { e -> Log.d("HELLO", "Error ask friends", e) }
    }

    fun removeFriends(onListen: () -> Unit, otherUserID: String, context: Context): ListenerRegistration {
        return fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/friends")
                .whereEqualTo("uid", otherUserID)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Get friends remove friend error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    querySnapshot!!.documents.forEach {
                        Log.d("HELLO", "suppr")
                        it.reference.delete().addOnFailureListener { e -> Log.d("HELLO", "Error remove friends", e) }
                        val tmp: Map<String, String> = hashMapOf("uid" to FirebaseAuth.getInstance().currentUser?.uid.toString())
                        fireStoreInstance.document("users/${otherUserID}/")
                                .collection("removedFriends")
                                .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                .set(tmp)
                                .addOnFailureListener { e -> Log.d("HELLO", "Error remove friends", e) }
                                .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                                val intent = Intent()
                                                intent.action = "onRemove"
                                                context.sendBroadcast(intent)
                                                onListen()
                                            }
                                        else {
                                            Log.d("LOGGER", "get failed with ", task.exception)
                                        }
                                    }
                                }
                        /*fireStoreInstance.document("users/"+otherUserID)
                                .collection("friends")
                                .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser?.uid.toString())
                                .get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        task.result!!.forEach { moi ->
                                            Log.d("HELLO", "suppr MOI")
                                            moi.reference.delete()
                                            val intent = Intent()
                                            intent.action = "onRemove"
                                            context.sendBroadcast(intent)
                                            onListen()
                                        }
                                    } else {
                                        Log.d("LOGGER", "get failed with ", task.exception)
                                    }
                                }*/
                    }
    }

    fun addFriends(userAsk: String) {
        var tmp: Map<String, String> = hashMapOf("uid" to userAsk)
        fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
                .collection("friends")
                .document(userAsk)
                .set(tmp)
                .addOnFailureListener { e -> Log.d("HELLO", "Error insert friends", e) }
        fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
                .collection("askFriends")
                .whereEqualTo("uid", userAsk)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result!!.forEach {
                            it.reference.delete()
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.exception)
                    }
                }
        val user = FirebaseAuth.getInstance()
        tmp = hashMapOf("uid" to user!!.uid.toString())
        fireStoreInstance.document("users/${userAsk}")
                .collection("friends")
                .document(user!!.uid.toString())
                .set(tmp)
                .addOnFailureListener { e -> Log.d("HELLO", "Error insert me inside my friend list", e) }
    }

    fun refuseFriend(userAsk: String) {
        fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
                .collection("askFriends")
                .whereEqualTo("uid", userAsk)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result!!.forEach {
                            it.reference.delete()
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.exception)
                    }
                }
    }

    fun removedFriend(context: Context) {
        fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
                .collection("removedFriends")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result!!.forEach {
                            fireStoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                                    ?: throw NullPointerException("UID is null.")}")
                                    .collection("friends")
                                    .whereEqualTo("uid", it["uid"])
                                    .get()
                                    .addOnCompleteListener { taskfriend ->
                                        if (taskfriend.isSuccessful) {
                                            taskfriend.result!!.forEach { itfriend->
                                                itfriend.reference.delete()
                                            }
                                            it.reference.delete()
                                        }
                                        else {
                                            Log.d("LOGGER", "get failed with ", task.exception)
                                        }
                                    }
                        }
                        val intent = Intent()
                        intent.action = "onRemove"
                        context.sendBroadcast(intent)
                    } else {
                        Log.d("LOGGER", "get failed with ", task.exception)
                    }
                }
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

    fun resetVote() {
        fireStoreInstance.collection("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}/reveilVote")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Reset Vote error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    for (document in querySnapshot!!.documents) {
                        document.reference.delete()
                    }
                }
    }
}