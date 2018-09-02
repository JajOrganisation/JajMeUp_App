package jajcompany.jajmeup.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.Models.AskingFriends
import jajcompany.jajmeup.Models.Vote
import jajcompany.jajmeup.R
import jajcompany.jajmeup.RecycleView.item.AskingFriendsItem
import jajcompany.jajmeup.RecycleView.item.UserItem
import jajcompany.jajmeup.Utils.FireStore
import jajcompany.jajmeup.Utils.StorageUtil
import jajcompany.jajmeup.Utils.YoutubeInformation
import jajcompany.jajmeup.glide.GlideApp
import kotlinx.android.synthetic.main.askingfriends_item.*
import kotlinx.android.synthetic.main.askingfriends_layout.*
import kotlinx.android.synthetic.main.community_layout.*
import java.util.*
import java.util.regex.Pattern

class AskingFriendsActivity : AppCompatActivity() {

    lateinit var databaseRef: DatabaseReference
    private lateinit var friendsListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var askingFriendsSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        friendsListenerRegistration = FireStore.addAskingFriendsListener(this, this::updateRecyclerView)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.askingfriends_layout)
    }

    private fun updateRecyclerView(items:List<Item>) {
        fun init() {
            askingfriends_list.apply {
                layoutManager = LinearLayoutManager(this@AskingFriendsActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    askingFriendsSection = Section(items)
                    add(askingFriendsSection)
                     setOnItemClickListener(onItemClick)
                    //setOnItemLongClickListener(onItemLongClick)*/
                }
            }
            shouldInitRecyclerView = false
        }
        fun updateItems() = askingFriendsSection.update(items)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()
    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is AskingFriendsItem) {
            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.accept_refuse_popup,null)
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
            val refuse = view.findViewById<Button>(R.id.button_refuse)
            val accept = view.findViewById<Button>(R.id.button_accept)
            val usernametext = view.findViewById<TextView>(R.id.addfriend_userName)
            val profilepicture = view.findViewById<ImageView>(R.id.addfriend_profile_picture)
            refuse.setOnClickListener{
                popupWindow.dismiss()
            }

            accept.setOnClickListener {
                popupWindow.dismiss()
                val askFriend = AskingFriends(item.user.uid, item.user.name, item.user.profilePicture)
               FireStore.addFriends(askFriend)
            }
            if (item.user.profilePicture != null) {
                GlideApp.with(this).load(StorageUtil.pathToReference(item.user.profilePicture.toString()))
                        .placeholder(R.drawable.ic_account_circle_black_24dp).into(profilepicture)
                Glide.with(this).load(StorageUtil.pathToReference(item.user.profilePicture.toString())).apply(RequestOptions.circleCropTransform()).into(profilepicture)
            }
            usernametext.text = item.user.name
            TransitionManager.beginDelayedTransition(askingfriends_layout)
            popupWindow.showAtLocation(
                    askingfriends_layout,
                    Gravity.CENTER,
                    0,
                    0
            )
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, AskingFriendsActivity::class.java)
            return intent
        }
    }
}