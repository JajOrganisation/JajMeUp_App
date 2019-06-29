package jajcompany.jajmeup.fragment


import android.content.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.OnItemLongClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.R
import jajcompany.jajmeup.RecycleView.item.UserItem
import jajcompany.jajmeup.glide.GlideApp
import jajcompany.jajmeup.models.User
import jajcompany.jajmeup.models.Vote
import jajcompany.jajmeup.utils.*
import kotlinx.android.synthetic.main.community_layout.*
import kotlinx.android.synthetic.main.community_list_header.view.*
import java.util.*
import java.util.regex.Pattern


class CommunityFragment : Fragment() {

    private var listTest: MutableList<String> = mutableListOf()
    private lateinit var userListenerRegistration: ListenerRegistration
    private lateinit var friendsListenerRegistration: ListenerRegistration
    private var listListenerRegistration: MutableList<ListenerRegistration> = mutableListOf()
    private lateinit var listFriendsListenerRegistration: ListenerRegistration
    private lateinit var searchListenerRegistration: ListenerRegistration
    private lateinit var removeListenerRegistration: ListenerRegistration
    private lateinit var removeFriendListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerViewWorld = true
    private var shouldInitRecyclerViewFriends = true
    private var shouldInitRecyclerViewSearch = false
    private var onSearch = false
    private lateinit var userSection: Section
    private lateinit var friendsSection: Section
    private lateinit var isFriend: ListenerRegistration
    private lateinit var _context: Context
    private var listFriends: MutableList<User> = mutableListOf()
    private var listFriendsView: MutableList<Item> = mutableListOf()
    private var listFriendsSection: MutableList<Item> = mutableListOf()
    private var broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            if (intent!!.action == "onAllFriends") {
                Log.d("HELLO", "onAllFriends "+intent.getStringArrayListExtra("uidList"))
                if (intent.getStringArrayListExtra("uidList") != null)
                    setAllFriendsListener(intent.getStringArrayListExtra("uidList"))
            }
            else if (intent.action == "onRemove") {
                Log.d("HELLO", "onRemove")
                unsetFriendsList()
                setUpdateListFriends()
            }
            else if (intent.action == "onNewFriend") {
                Log.d("HELLO", "onNewFriend")
                unsetListWorld()
                setUpdateListWorld()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.community_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        //detectPref()
        header_friends.header_communauty.text = getString(R.string.label_header_friends_list_community_string)

        header_friends.header_communauty.setOnClickListener {
            if(friends_list.visibility == View.GONE)
                friends_list.visibility = View.VISIBLE
            else
                friends_list.visibility = View.GONE
        }
        searchusers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText == "") {
                    onSearch = false
                    header_friends.visibility = View.VISIBLE
                    friends_list.visibility = View.VISIBLE
                    header_world.visibility = View.VISIBLE
                    community_list.visibility = View.VISIBLE
                    search_list.visibility = View.GONE
                    unsetSearch()
                    setUpdateListFriends()
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    if (sharedPreferences.getString("visibility_preference", "WORLD") == "WORLD") {
                        setUpdateListWorld()
                    }
                }
                else {
                    onSearch = true
                    shouldInitRecyclerViewSearch = true
                    header_friends.visibility = View.GONE
                    friends_list.visibility = View.GONE
                    header_world.visibility = View.GONE
                    community_list.visibility = View.GONE
                    search_list.visibility = View.VISIBLE
                    unsetFriendsList()
                    unsetListWorld()
                    setSearch(newText)
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                return false
            }
        })
    }

    override fun onResume() {
        detectPref()
        super.onResume()
        //unsetListWorld()
        //unsetFriendsList()
        //unsetFriends()
        //unsetRemoveFriends()
       // setUpdateListWorld()
        //setUpdateListFriends()
        //setRemoveFriends()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
        if (sharedPreferences.getBoolean("on_wakeup", false)){
            Log.d("HELLO", "OK")
            community_layout.post{
                Alarm.deleteAlarm()
                sharedPreferences.edit().putString("hours_clock", "-11:-11").apply()
            }
        }
        else if (sharedPreferences.getBoolean("on_wakeup_my_alarm", false)){
            sharedPreferences.edit().putBoolean("on_wakeup_my_alarm", false).apply()
            sharedPreferences.edit().putString("hours_clock", "-11:-11").apply()
            Alarm.deleteAlarm()
        }
        if (!Jajinternet.getStatusInternet(context)) {
            Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            setUpdateListFriends()
            setUpdateListWorld()
            setRemoveFriends()
            unsetRemoveFriends()
            unsetFriendsList()
            unsetListWorld()
        }catch (e: Exception) {

        }
        shouldInitRecyclerViewFriends = true
        shouldInitRecyclerViewWorld = true
    }

    private fun updateRecyclerViewWorld(items:List<Item>) {
        fun initWorld() {
            try {
                community_list.apply {
                    layoutManager = LinearLayoutManager(_context)
                    adapter = GroupAdapter<ViewHolder>().apply {
                        userSection = Section(items)
                        add(userSection)
                        setOnItemClickListener(onItemClick)
                        setOnItemLongClickListener(onItemLongClick)
                    }
                }
                shouldInitRecyclerViewWorld = false
            } catch (e: Exception) {
                Log.d("HELLO", "Error update world", e)
            }
        }
        fun updateItemsWorld() = userSection.update(items)

        if (shouldInitRecyclerViewWorld)
            initWorld()
        else {
            try {
                updateItemsWorld()
            } catch (e: Exception) {
                Log.d("HELLO", "Error update world", e)
            }
        }
    }

    private fun updateRecyclerViewFriends(items:List<Item>/*, uid: String*/) {
        fun initFriends() {
            try {
               // listFriends.add(uid)
                listFriendsSection.add(items[0])
                friends_list.apply {
                    layoutManager = LinearLayoutManager(_context)
                    adapter = GroupAdapter<ViewHolder>().apply {
                        friendsSection = Section(items)
                        add(friendsSection)
                        setOnItemClickListener(onItemClick)
                        setOnItemLongClickListener(onItemLongClickFriend)
                    }
                }
                shouldInitRecyclerViewFriends = false
            } catch (e: Exception) {
                Log.e("Error", "Error update friends")
            }
        }
        fun updateItemsFriends() = friendsSection.update(items)

       /* fun updateItemsFriends() {
            try {
                Log.d("HELLO", "on check "+friendsSection.getPosition(items[0]))
                if (items.isNotEmpty()) {
                    if (listFriends.indexOf(uid) == -1) {
                        listFriends.add(uid)
                        listFriendsSection.add(items[0])
                        return friendsSection.update(listFriendsSection)
                    }
                    else {
                        listFriendsSection[listFriends.indexOf(uid)] = items[0]
                        return friendsSection.update(listFriendsSection)
                    }
                }
            }catch (e: Exception) {
                resetall()
            }
        }*/

        if (shouldInitRecyclerViewFriends)
            initFriends()
        else {
            try {
                updateItemsFriends()
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
                if (sharedPreferences.getString("visibility_preference", "WORLD") == "WORLD") {
                    unsetListWorld()
                    setUpdateListWorld()
                }
            } catch (e: Exception) {
                Log.e("Error", "Error update friends")
            }
        }
    }

    private fun updateListFriends(items:List<String>) {
        Log.d("HELLO", "Belle liste"+items)
        setUpdateFriends()
        //unsetFriends()
        //setUpdateFriends()
    }

    private fun updateRecyclerViewSearch(items:List<Item>) {
        fun initSearch() {
            search_list.apply {
                layoutManager = LinearLayoutManager(_context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    userSection = Section(items)
                    add(userSection)
                    setOnItemClickListener(onItemSearchClick)
                }
            }
            shouldInitRecyclerViewSearch = false
        }
        fun updateItemsSearch() = userSection.update(items)

        if (shouldInitRecyclerViewSearch)
            initSearch()
        else
        {
            try {
                updateItemsSearch()
            } catch (e: Exception) {
                Log.e("Error", "Error update search")
            }
        }
    }

    private fun getIsMyFriend(result: UserItem) {
        if(result.user.name != "")
            showPopVote(result)
    }

    private val onItemLongClick = OnItemLongClickListener { item, _ ->

        if (item is UserItem) {
            if (Jajinternet.getStatusInternet(context)) {
                showPopAddFriend(item)
            }
            else {
                Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }
        true
    }

    private fun notifRemove() {
        FireStore.removeListener(removeListenerRegistration)
        resetall()
    }

    private val onItemLongClickFriend = OnItemLongClickListener { item, _ ->

        if (item is UserItem) {
            if (Jajinternet.getStatusInternet(context)) {
                showPopRemove(item)
            }
            else{
                Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }
        true
    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is UserItem) {
            if (Jajinternet.getStatusInternet(context)) {
                var flag = true
                if (onSearch) {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
                    if (sharedPreferences.getString("visibility_preference", "WORLD") == "FRIENDS") {
                        flag = false
                        isFriend = FireStore.isFriend(this.activity!!, item, this::getIsMyFriend)
                    }
                }
                if (flag) {
                    showPopVote(item)
                }
            }
            else{
                Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }
    }

    private val onItemSearchClick = OnItemClickListener { item, view ->
        if (item is UserItem) {
            if (Jajinternet.getStatusInternet(context)) {
                val textTest = view.findViewById<TextView>(R.id.isFriendSearch)
                if (textTest.text == "false") {
                    showPopSearch(item, false)
                } else {
                    showPopSearch(item, true)
                }
            }
            else {
                Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setUpdateListWorld() {
        Log.d("HELLO", "set world")
        try {
            FireStore.getLastDateRandom {end ->
                if (end == 0L){
                    try {
                        userListenerRegistration = FireStore.getUsers(this.activity!!, 0, this::updateRecyclerViewWorld)
                    } catch (e: Exception){

                    }
                }
                else
                    FireStore.getStartDateRandom { start ->
                        try {
                            val randomDate = (start..end).random()
                            userListenerRegistration = FireStore.getUsers(this.activity!!, randomDate, this::updateRecyclerViewWorld)
                        } catch (e: Exception){

                        }
                    }
            }

        }catch (e: Exception){

        }
       // userListenerRegistration = FireStore.addUsersListener(this.activity!!, this::updateRecyclerViewWorld)
    }

    fun setUpdateFriends() {
        try {
            context!!.registerReceiver(broadCastReceiver, IntentFilter("onAllFriends"))
            FireStore.getAllFriendUID(this.activity!!)
        } catch (e: Exception){

        }
        //friendsListenerRegistration = FireStore.getAllFriendUID(this.activity!!, this::updateRecyclerViewFriends)
    }

    fun prepareViewFriend(items:User) {
        var count = 0
        var flag = false
        Log.d("HELLO", "TestFriend "+items.name)
        try {
            for (current in listFriends) {
                if (current.uid == items.uid) {
                    Log.d("HELLO", "Count " + count.toString())
                    listFriendsView[count] = UserItem(items, items.uid, this.activity!!)
                    flag = true
                }
                count += 1
            }
            if (!flag && items.uid != "") {
                listFriends.add(items)
                listFriendsView.add(UserItem(items, items.uid, this.activity!!))
            }
            updateRecyclerViewFriends(listFriendsView)
        }catch (e:Exception) {}
    }

    fun setAllFriendsListener(test: List<String>) {
        context!!.unregisterReceiver(broadCastReceiver)
        if (test.isNotEmpty()) {
            if (test[0] != "Nothing") {
                listListenerRegistration = mutableListOf()
                for (current in test) {
                    Log.d("Hello", "onAllFriends add "+current)
                    if (!listTest.contains(current)) {
                        listListenerRegistration.add(FireStore.addFriendsListener(this.activity!!, current, this::prepareViewFriend))
                        listTest.add(current)
                    }
                }
            }
        }
    }

    fun updateRemoveFriends(items: List<String>) {
        try {
            context!!.registerReceiver(broadCastReceiver, IntentFilter("onRemove"))
            FireStore.removedFriend(this.activity!!)
        } catch (e: Exception){

        }
    }

    fun setUpdateListFriends() {
        context!!.registerReceiver(broadCastReceiver, IntentFilter("onNewFriend"))
        listFriendsListenerRegistration = FireStore.newFriendsListener(this::updateListFriends)
    }

    fun setSearch(toSearch: String) {
        searchListenerRegistration = FireStore.searchUser(this.activity!!, this::updateRecyclerViewSearch, toSearch)
    }

    fun setRemoveFriends() {
        removeFriendListenerRegistration = FireStore.addRemovedFriendsListener(this::updateRemoveFriends)
    }

    fun unsetRemoveFriends() {
        FireStore.removeListener(removeFriendListenerRegistration)
    }

    fun unsetSearch() {
        FireStore.removeListener(searchListenerRegistration)
    }

    fun unsetListWorld() {
        try {
            shouldInitRecyclerViewWorld = true
            FireStore.removeListener(userListenerRegistration)
        }catch (e: Exception) {

        }
    }

    fun unsetFriendsList() {
        //friendsSection = Section()
        shouldInitRecyclerViewFriends = true
        FireStore.removeListener(listFriendsListenerRegistration)
    }

    fun unsetFriends() {
        FireStore.removeListener(friendsListenerRegistration)
    }

    fun resetall() {
        unsetListWorld()
        unsetFriendsList()
        unsetRemoveFriends()
        setUpdateListWorld()
        setUpdateListFriends()
        setRemoveFriends()
    }

    private fun detectPref() {
        try {
            unsetFriendsList()
            unsetRemoveFriends()
        }catch (e: java.lang.Exception){

        }
        setRemoveFriends()
        setUpdateListFriends()
        //setUpdateListWorld()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
        if (sharedPreferences.getString("visibility_preference", "WORLD") == "WORLD") {
            setUpdateListWorld()
            header_world.visibility = View.VISIBLE
            community_list.visibility = View.VISIBLE
            header_world.setRandomImageButton.visibility = View.VISIBLE
            header_world.header_communauty.text = getString(R.string.label_header_world_list_community_string)
            header_world.header_communauty.setOnClickListener {

                if (community_list.visibility == View.GONE)
                    community_list.visibility = View.VISIBLE
                else
                    community_list.visibility = View.GONE
            }

            header_world.setRandomImageButton.setOnClickListener {
                //FireStore.setRandomUserNumber(_context)
                unsetListWorld()
                setUpdateListWorld()
            }
        }
        else {
            header_world.visibility = View.GONE
            community_list.visibility = View.GONE
            unsetListWorld()
        }
    }

    private fun showPopAddFriend(item:UserItem) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.addfriend_popup_layout,null)
        val popupWindow = PopupWindow(
                view,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.TOP
        popupWindow.enterTransition = slideIn
        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        popupWindow.exitTransition = slideOut
        popupWindow.isFocusable = true
        val closepop = view.findViewById<Button>(R.id.button_addfriend_closepop)
        val usernametext = view.findViewById<TextView>(R.id.addfriend_userName)
        val profilepicture = view.findViewById<ImageView>(R.id.addfriend_profile_picture)
        val askfriend = view.findViewById<Button>(R.id.button_addfriendpop)
        closepop.setOnClickListener{
            popupWindow.dismiss()
        }
        askfriend.setOnClickListener {
            FireStore.getCurrentUser {myuser ->
                if ( myuser.profilePicture != null) {
                    val user = FirebaseAuth.getInstance()
                    FireStore.askFriends(user!!.uid.toString(), item.userId)

                }
            }
            popupWindow.dismiss()
        }
        if (item.user.profilePicture != null) {
            GlideApp.with(this).load(StorageUtil.pathToReference(item.user.profilePicture.toString()))
                    .placeholder(R.drawable.ic_account_circle_black_24dp).into(profilepicture)
            Glide.with(this).load(StorageUtil.pathToReference(item.user.profilePicture.toString())).apply(RequestOptions.circleCropTransform()).into(profilepicture)
        }
        usernametext.text = item.user.name
        popupWindow.showAtLocation(
                community_layout,
                Gravity.CENTER,
                0,
                0
        )
    }

    private fun showPopRemove(item: UserItem) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.removefriend_popup_layout,null)
        val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.TOP
        popupWindow.enterTransition = slideIn
        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        popupWindow.exitTransition = slideOut
        popupWindow.isFocusable = true
        val closepop = view.findViewById<Button>(R.id.button_removefriend_closepop)
        val usernametext = view.findViewById<TextView>(R.id.removefriend_userName)
        val profilepicture = view.findViewById<ImageView>(R.id.removefriend_profile_picture)
        val removefriend = view.findViewById<Button>(R.id.button_removefriendpop)
        closepop.setOnClickListener{
            popupWindow.dismiss()
        }
        removefriend.setOnClickListener {
            context!!.registerReceiver(broadCastReceiver, IntentFilter("onRemove"))
            FireStore.getCurrentUser {myuser ->
                    removeListenerRegistration = FireStore.removeFriends(this::notifRemove, item.userId, this.activity!!)
                    friendsSection.remove(item)
            }
            popupWindow.dismiss()
        }
        if (item.user.profilePicture != null) {
            GlideApp.with(this).load(StorageUtil.pathToReference(item.user.profilePicture.toString()))
                    .placeholder(R.drawable.ic_account_circle_black_24dp).into(profilepicture)
            Glide.with(this).load(StorageUtil.pathToReference(item.user.profilePicture.toString())).apply(RequestOptions.circleCropTransform()).into(profilepicture)
        }
        usernametext.text = item.user.name
        popupWindow.showAtLocation(
                community_layout,
                Gravity.CENTER,
                0,
                0
        )
    }

    private fun showPopVote(item: UserItem) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.vote_popup_layout, null)
        val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        val slideIn = Slide()
        slideIn.slideEdge = Gravity.TOP
        popupWindow.enterTransition = slideIn
        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        popupWindow.exitTransition = slideOut
        popupWindow.isFocusable = true
        val closepop = view.findViewById<Button>(R.id.button_closepop)
        val votepop = view.findViewById<Button>(R.id.button_votepop)
        val edityt = view.findViewById<EditText>(R.id.youtubelinkpop)
        val editmess = view.findViewById<EditText>(R.id.messagepop)
        val labelmess = view.findViewById<TextView>(R.id.message_label)
        val labelvideoname = view.findViewById<TextView>(R.id.youtube_video_nom)
        val imagebutton = view.findViewById<ImageButton>(R.id.pastebutton)
        labelmess.text = getString(R.string.label_write_message_string)+" "+item.user.name
       // labelmess.setText(getString(R.string.label_write_message_string)+ item.user.name)
        closepop.setOnClickListener {
            popupWindow.dismiss()
        }
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
        val compiledPattern = Pattern.compile(pattern)
        votepop.setOnClickListener {
            val matcher = compiledPattern.matcher(edityt.text.toString())
            if (matcher.find()) {
                val user = FirebaseAuth.getInstance().currentUser
                val titlevideo = YoutubeInformation.getTitleQuietly(matcher.group())
                if (titlevideo != "ERROR") {
                    val vote = Vote(edityt.text.toString(), titlevideo, user?.uid.toString(), editmess.text.toString(), Calendar.getInstance().time)
                    FireStore.sendVote(vote, item.user.uid)
                    popupWindow.dismiss()
                    Toast.makeText(activity, getString(R.string.vote_pour) +" "+item.user.name, Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(activity, getString(R.string.lien_yt_invalide), Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(activity, getString(R.string.lien_yt_invalide), Toast.LENGTH_LONG).show()
            }
        }
        val myClipboard: ClipboardManager? = activity!!.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager?
        val primary = myClipboard?.primaryClip
        val itemPaste = primary?.getItemAt(0)
        if (PreferenceManager.getDefaultSharedPreferences(this.activity).getString("current_link", "123456") != "123456") {
            if (compiledPattern.matcher(PreferenceManager.getDefaultSharedPreferences(this.activity).getString("current_link", "123456")).find()) {
                edityt.setText(PreferenceManager.getDefaultSharedPreferences(this.activity).getString("current_link", "123456"))
                labelvideoname.visibility = View.VISIBLE
                labelvideoname.text = YoutubeInformation.getTitleQuietly(YoutubeInformation.getIDFromURL(PreferenceManager.getDefaultSharedPreferences(this.activity).getString("current_link", "123456"))).take(50)
            }
        }
        else if (compiledPattern.matcher(itemPaste!!.text.toString()).find()) {
            Toast.makeText(context, "Lien copié depuis le presse-papier", Toast.LENGTH_SHORT).show()
            edityt.setText(itemPaste.text.toString())
            labelvideoname.visibility = View.VISIBLE
            labelvideoname.text = YoutubeInformation.getTitleQuietly(itemPaste.text.toString()).take(50)
        }

        imagebutton.setOnClickListener {
            val myClipboardd: ClipboardManager? = activity!!.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager?
            val primaryy = myClipboardd?.primaryClip
            val itemPastee = primaryy?.getItemAt(0)
            if (compiledPattern.matcher(itemPastee!!.text.toString()).find()) {
                Toast.makeText(context, "Lien copié depuis le presse-papier", Toast.LENGTH_SHORT).show()
                edityt.setText(itemPastee.text.toString())
                labelvideoname.visibility = View.VISIBLE
                labelvideoname.text = YoutubeInformation.getTitleQuietly(YoutubeInformation.getIDFromURL(itemPastee.text.toString())).take(50)
            }
            else
                Toast.makeText(context, "Aucun lien dans le presse-papier", Toast.LENGTH_SHORT).show()
        }

        TransitionManager.beginDelayedTransition(community_layout)
        popupWindow.showAtLocation(
                community_layout,
                Gravity.CENTER,
                0,
                0
        )
    }
    fun showPopSearch(item: UserItem, isFriend: Boolean) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.search_popup_layout, null)
        val popupWindow = PopupWindow(
                view,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.TOP
        popupWindow.enterTransition = slideIn
        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        popupWindow.exitTransition = slideOut
        popupWindow.isFocusable = true
        val closepop = view.findViewById<Button>(R.id.button_closepop)
        val votepop = view.findViewById<Button>(R.id.button_votepop)
        val addfriend = view.findViewById<Button>(R.id.button_addfriendpop)
        val profilepicture = view.findViewById<ImageView>(R.id.search_profile_picture_pop)

        votepop.setOnClickListener {
            popupWindow.dismiss()
            showPopVote(item)
        }
        if (!isFriend) {
            addfriend.visibility = View.VISIBLE
            addfriend.setOnClickListener {
                FireStore.getCurrentUser {myuser ->
                    if ( myuser.profilePicture != null) {
                        val user = FirebaseAuth.getInstance()
                        FireStore.askFriends(user!!.uid.toString(), item.userId)

                    }
                }
                popupWindow.dismiss()
            }
        }
        closepop.setOnClickListener {
            popupWindow.dismiss()
        }
        if (item.user.profilePicture != null) {
            GlideApp.with(this).load(StorageUtil.pathToReference(item.user.profilePicture.toString()))
                    .placeholder(R.drawable.ic_account_circle_black_24dp).into(profilepicture)
            Glide.with(this).load(StorageUtil.pathToReference(item.user.profilePicture.toString())).apply(RequestOptions.circleCropTransform()).into(profilepicture)
        }
        TransitionManager.beginDelayedTransition(community_layout)
        popupWindow.showAtLocation(
                community_layout,
                Gravity.CENTER,
                0,
                0
        )
    }

}