package jajcompany.jajmeup.fragment

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import jajcompany.jajmeup.activity.YouTubeJAJActivity
import jajcompany.jajmeup.models.Vote
import jajcompany.jajmeup.R
import jajcompany.jajmeup.RecycleView.item.UserItem
import jajcompany.jajmeup.utils.FireStore
import jajcompany.jajmeup.utils.StorageUtil
import jajcompany.jajmeup.utils.YoutubeInformation
import jajcompany.jajmeup.utils.YoutubeInformation.getTitleQuietly
import jajcompany.jajmeup.glide.GlideApp
import kotlinx.android.synthetic.main.community_layout.*
import kotlinx.android.synthetic.main.community_list_header.view.*
import java.util.*
import java.util.regex.Pattern


class CommunityFragment : Fragment() {

    private lateinit var userListenerRegistration: ListenerRegistration
    private lateinit var friendsListenerRegistration: ListenerRegistration
    private lateinit var searchListenerRegistration: ListenerRegistration
    private lateinit var removeListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerViewWorld = true
    private var shouldInitRecyclerViewFriends = true
    private var shouldInitRecyclerViewSearch = false
    private var onSearch = false
    private lateinit var userSection: Section
    private lateinit var friendsSection: Section
    private lateinit var isFriend: ListenerRegistration
    private lateinit var _context: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater?.inflate(R.layout.community_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        detectPref()
        header_friends.header_communauty.text = "Amis"

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
        unsetListWorld()
        unsetFriendsList()
        setUpdateListWorld()
        setUpdateListFriends()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
        if (sharedPreferences.getBoolean("on_wakeup", false)){
            showPopOnWakeUp()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsetFriendsList()
        unsetListWorld()
        shouldInitRecyclerViewFriends = true
        shouldInitRecyclerViewWorld = true
    }

    fun updateRecyclerViewWorld(items:List<Item>) {
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
                Log.e("Error", "Error update world")
            }
        }
        fun updateItemsWorld() = userSection.update(items)

        if (shouldInitRecyclerViewWorld)
            initWorld()
        else {
            try {
                updateItemsWorld()
            } catch (e: Exception) {
                Log.e("Error", "Error update world")
            }
        }
    }

    private fun updateRecyclerViewFriends(items:List<Item>) {
        Log.d("HELLO", items.toString())
        fun initFriends() {
            try {
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

    private fun updateRecyclerViewSearch(items:List<Item>) {
        fun initWorld() {
            search_list.apply {
                layoutManager = LinearLayoutManager(_context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    userSection = Section(items)
                    add(userSection)
                    setOnItemClickListener(onItemClick)
                    setOnItemLongClickListener(onItemLongClick)
                }
            }
            shouldInitRecyclerViewSearch = false
        }
        fun updateItemsWorld() = userSection.update(items)

        if (shouldInitRecyclerViewSearch)
            initWorld()
        else
        {
            try {
                updateItemsWorld()
            } catch (e: Exception) {
                Log.e("Error", "Error update search")
            }
        }
    }

    private fun getIsMyFriend(result: UserItem) {
        if(result.user.name != "")
            showPopVote(result)
    }

    private val onItemLongClick = OnItemLongClickListener { item, view ->

        if (item is UserItem) {
            if (onSearch) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
                if (sharedPreferences.getString("visibility_preference", "WORLD") == "FRIENDS") {
                    true
                }
            }
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.addfriend_popup_layout,null)
            val popupWindow = PopupWindow(
                    view, // Custom view to show in popup window
                    LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                    LinearLayout.LayoutParams.WRAP_CONTENT // Window height
            )
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
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
                val myuser = FireStore.getCurrentUser {myuser ->
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
        true
    }

    private fun notifRemove() {
        FireStore.removeListener(removeListenerRegistration)
        testresetall()
    }

    private val onItemLongClickFriend = OnItemLongClickListener { item, view ->

        if (item is UserItem) {
            if (onSearch) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
                if (sharedPreferences.getString("visibility_preference", "WORLD") == "FRIENDS") {
                    true
                }
            }
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
            slideOut.slideEdge = Gravity.RIGHT
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
                FireStore.getCurrentUser {myuser ->
                    if ( myuser.profilePicture != null) {
                        removeListenerRegistration = FireStore.removeFriends(this::notifRemove, item.userId)
                        friendsSection.remove(item)
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
        true
    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is UserItem) {
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
    }

    fun setUpdateListWorld() {
        userListenerRegistration = FireStore.getUsers(this.activity!!, this::updateRecyclerViewWorld)
       // userListenerRegistration = FireStore.addUsersListener(this.activity!!, this::updateRecyclerViewWorld)
    }

    fun setUpdateListFriends() {
        friendsListenerRegistration = FireStore.addFriendsListener(this.activity!!, this::updateRecyclerViewFriends)
    }

    fun setSearch(toSearch: String) {
        searchListenerRegistration = FireStore.searchUser(this.activity!!, this::updateRecyclerViewSearch, toSearch)
    }
    fun unsetSearch() {
        FireStore.removeListener(searchListenerRegistration)
    }

    fun unsetListWorld() {
        FireStore.removeListener(userListenerRegistration)
    }

    fun unsetFriendsList() {
        //friendsSection = Section()
        FireStore.removeListener(friendsListenerRegistration)
    }

    fun testresetall() {
        unsetListWorld()
        unsetFriendsList()
        setUpdateListWorld()
        setUpdateListFriends()
    }

    private fun detectPref() {
        setUpdateListFriends()
        setUpdateListWorld()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
        if (sharedPreferences.getString("visibility_preference", "WORLD") == "WORLD") {
            setUpdateListWorld()
            header_world.visibility = View.VISIBLE
            community_list.visibility = View.VISIBLE
            header_world.setRandomImageButton.visibility = View.VISIBLE
            header_world.header_communauty.text = "Tout le monde"
            header_world.header_communauty.setOnClickListener {

                if (community_list.visibility == View.GONE)
                    community_list.visibility = View.VISIBLE
                else
                    community_list.visibility = View.GONE
            }

            header_world.setRandomImageButton.setOnClickListener {
                FireStore.setRandomUserNumber(_context)
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
        slideOut.slideEdge = Gravity.RIGHT
        popupWindow.exitTransition = slideOut
        popupWindow.isFocusable = true
        val closepop = view.findViewById<Button>(R.id.button_closepop)
        val votepop = view.findViewById<Button>(R.id.button_votepop)
        val edityt = view.findViewById<EditText>(R.id.youtubelinkpop)
        val editmess = view.findViewById<EditText>(R.id.messagepop)
        val labelmess = view.findViewById<TextView>(R.id.message_label)
        labelmess.setText("Écrit un message pour " + item.user.name)
        closepop.setOnClickListener {
            popupWindow.dismiss()
        }

        votepop.setOnClickListener {
            val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
            val compiledPattern = Pattern.compile(pattern)
            val matcher = compiledPattern.matcher(edityt.text.toString())
            if (matcher.find()) {
                val user = FirebaseAuth.getInstance().currentUser
                val vote = Vote(matcher.group(), YoutubeInformation.getTitleQuietly(matcher.group()), user?.uid.toString(), editmess.text.toString(), Calendar.getInstance().time)
                FireStore.sendVote(vote, item.user.uid)
                popupWindow.dismiss()
                Toast.makeText(activity, "Tu as voté pour " + item.user.name, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, "Invalid link", Toast.LENGTH_LONG).show()
            }
        }
        if (PreferenceManager.getDefaultSharedPreferences(context).getString("current_link", "123456") != "123456") {
            val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
            val compiledPattern = Pattern.compile(pattern)
            val matcher = compiledPattern.matcher(PreferenceManager.getDefaultSharedPreferences(context).getString("current_link", "123456"))
            if (matcher.find()) {
                edityt.setText(PreferenceManager.getDefaultSharedPreferences(context).getString("current_link", "123456"))
            }
        }
        TransitionManager.beginDelayedTransition(community_layout)
        popupWindow.showAtLocation(
                community_layout,
                Gravity.CENTER,
                0,
                0
        )
    }
    private fun showPopOnWakeUp() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
        sharedPreferences.edit().putString("user_wakeup", YouTubeJAJActivity.votant).apply()
        sharedPreferences.edit().putString("message_wakeup", YouTubeJAJActivity.message).apply()
        sharedPreferences.edit().putString("link_wakeup", YouTubeJAJActivity.lien).apply()
        sharedPreferences.edit().putBoolean("on_wakeup", false).apply()
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.wakeup_popup_layout, null)
        val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        val slideIn = Slide()
        slideIn.slideEdge = Gravity.TOP
        popupWindow.enterTransition = slideIn
        val slideOut = Slide()
        slideOut.slideEdge = Gravity.RIGHT
        popupWindow.exitTransition = slideOut
        popupWindow.isFocusable = true
        val closepop = view.findViewById<Button>(R.id.button_closepop_wakeup)
        val response = view.findViewById<Button>(R.id.button_response_wakeup)
        val labelyt = view.findViewById<TextView>(R.id.yt_wakeup)
        val labelvotant = view.findViewById<TextView>(R.id.votant_wakeup)
        val labelmess = view.findViewById<TextView>(R.id.message_wakeup)
        closepop.setOnClickListener {
            popupWindow.dismiss()
        }

        labelyt.text = "Avec cette vidéo :\n"+getTitleQuietly(sharedPreferences.getString("link_wakeup", ""))

        labelvotant.text = sharedPreferences.getString("user_wakeup", "")+" t'as réveillé"
        if (sharedPreferences.getString("message_wakeup", "") != "") {
            labelmess.text = sharedPreferences.getString("message_wakeup", "")
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