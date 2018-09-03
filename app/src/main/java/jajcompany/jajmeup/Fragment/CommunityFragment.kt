package jajcompany.jajmeup.Fragment

import android.support.v4.app.Fragment
import android.os.Bundle
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
import com.google.firebase.database.*
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.OnItemLongClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.Models.AskingFriends
import jajcompany.jajmeup.Models.Vote
import jajcompany.jajmeup.R
import jajcompany.jajmeup.RecycleView.item.UserItem
import jajcompany.jajmeup.Utils.FireStore
import jajcompany.jajmeup.Utils.FireStore.askFriends
import jajcompany.jajmeup.Utils.StorageUtil
import kotlinx.android.synthetic.main.community_layout.*
import jajcompany.jajmeup.Utils.YoutubeInformation
import jajcompany.jajmeup.glide.GlideApp
import kotlinx.android.synthetic.main.community_list_header.view.*
import kotlinx.android.synthetic.main.community_list_item.*
import java.util.*
import java.util.regex.Pattern


class CommunityFragment : Fragment() {

    lateinit var databaseRef: DatabaseReference
    private lateinit var userListenerRegistration: ListenerRegistration
    private lateinit var friendsListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerViewWorld = true
    private var shouldInitRecyclerViewFriends = true
    private lateinit var userSection: Section
    private lateinit var friendsSection: Section

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setUpdateList()
        //friendsListenerRegistration = FireStore.addFriendsListener(this.activity!!, this::updateRecyclerViewFriends)
        //userListenerRegistration = FireStore.addUsersListener(this.activity!!, this::updateRecyclerViewWorld)
       /* if (arguments != null) {
            Log.d("YOUTUBE_FRAGMENT", arguments.getString("link"))
        }*/

        return inflater?.inflate(R.layout.community_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header_friends.header_communauty.text = "Amis"
        header_world.header_communauty.text = "Tout le monde"

        header_friends.header_communauty.setOnClickListener {
            if(friends_list.visibility == View.GONE)
                friends_list.visibility = View.VISIBLE
            else
                friends_list.visibility = View.GONE
        }

        header_world.header_communauty.setOnClickListener {

            if(community_list.visibility == View.GONE)
                community_list.visibility = View.VISIBLE
            else
                community_list.visibility = View.GONE
        }
        searchusers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText == "") {
                    header_friends.visibility = View.VISIBLE
                    friends_list.visibility = View.VISIBLE
                    header_world.visibility = View.VISIBLE
                    community_list.visibility = View.VISIBLE
                    unsetSearch()
                    setUpdateList()
                }
                else {
                    header_friends.visibility = View.GONE
                    friends_list.visibility = View.GONE
                    header_world.visibility = View.GONE
                    community_list.visibility = View.VISIBLE
                    removeUpdateList()
                    setSearch(newText)
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                //Task HERE
                return false
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeUpdateList()
        shouldInitRecyclerViewFriends = true
        shouldInitRecyclerViewWorld = true
    }

    fun updateRecyclerViewWorld(items:List<Item>) {
        fun initWorld() {
            community_list.apply {
                layoutManager = LinearLayoutManager(this@CommunityFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    userSection = Section(items)
                    add(userSection)
                    setOnItemClickListener(onItemClick)
                    setOnItemLongClickListener(onItemLongClick)
                }
            }
            shouldInitRecyclerViewWorld = false
        }
        fun updateItemsWorld() = userSection.update(items)

        if (shouldInitRecyclerViewWorld)
            initWorld()
        else
            updateItemsWorld()
    }

    private fun updateRecyclerViewFriends(items:List<Item>) {
        fun initFriends() {
            friends_list.apply {
                layoutManager = LinearLayoutManager(this@CommunityFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    friendsSection = Section(items)
                    add(friendsSection)
                    setOnItemClickListener(onItemClick)
                    //setOnItemLongClickListener(onItemLongClick)
                }
            }
            shouldInitRecyclerViewFriends = false
        }
        fun updateItemsFriends() = friendsSection.update(items)

        if (shouldInitRecyclerViewFriends)
            initFriends()
        else
            updateItemsFriends()
    }

    private val onItemLongClick = OnItemLongClickListener { item, view ->
        if (item is UserItem) {
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
                        val profilepath = myuser.profilePicture
                        val myprofil = AskingFriends(user!!.uid.toString(), myuser.name, profilepath)
                        FireStore.askFriends(myprofil, item.userId)

                    }
                }
               // val myprofil = AskingFriends(user!!.uid, user?.displayName.toString(), profilepath)

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
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.vote_popup_layout,null)
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
            labelmess.setText("Écrit un message pour "+item.user.name)
            closepop.setOnClickListener{
                popupWindow.dismiss()
            }

            votepop.setOnClickListener {
                val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
                val compiledPattern = Pattern.compile(pattern)
                val matcher = compiledPattern.matcher(edityt.text.toString())
                if (matcher.find()) {
                    val user = FirebaseAuth.getInstance().currentUser
                    val vote = Vote(matcher.group(), YoutubeInformation.getTitleQuietly(matcher.group()), user?.displayName.toString(), editmess.text.toString(), Calendar.getInstance().time)
                    FireStore.sendVote(vote, item.userId)
                    popupWindow.dismiss()
                    Toast.makeText(activity,"Tu as voté pour "+item.user.name, Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(activity, "Invalid link", Toast.LENGTH_LONG).show()
                }
            }
            if (arguments?.getString("link") != null){
                val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
                val compiledPattern = Pattern.compile(pattern)
                val matcher = compiledPattern.matcher(arguments?.getString("link").toString())
                if (matcher.find()) {
                    edityt.setText(arguments?.getString("link").toString())

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
    }

    fun setUpdateList() {
        friendsListenerRegistration = FireStore.addFriendsListener(this.activity!!, this::updateRecyclerViewFriends)
        userListenerRegistration = FireStore.addUsersListener(this.activity!!, this::updateRecyclerViewWorld)
    }

    fun setSearch(toSearch: String) {
        userListenerRegistration = FireStore.searchUser(this.activity!!, this::updateRecyclerViewWorld, toSearch)
    }

    fun unsetSearch() {
        FireStore.removeListener(userListenerRegistration)
    }

    fun removeUpdateList() {
        FireStore.removeListener(userListenerRegistration)
        FireStore.removeListener(friendsListenerRegistration)
    }

     companion object {

        fun newInstance(link: String): Fragment {
            val args = Bundle()
            args.putString("link", link)
            val fragment = CommunityFragment()
            fragment.arguments = args
            return fragment
        }
    }

    fun getGreeting(){
        databaseRef.child("name").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                println("BONJOUR: ${snapshot.value}")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}