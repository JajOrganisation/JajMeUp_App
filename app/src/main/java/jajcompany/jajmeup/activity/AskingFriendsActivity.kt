package jajcompany.jajmeup.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.R
import jajcompany.jajmeup.RecycleView.item.AskingFriendsItem
import jajcompany.jajmeup.glide.GlideApp
import jajcompany.jajmeup.utils.FireStore
import jajcompany.jajmeup.utils.Jajinternet
import jajcompany.jajmeup.utils.StorageUtil
import kotlinx.android.synthetic.main.askingfriends_layout.*

class AskingFriendsActivity : AppCompatActivity() {

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
                    //setOnItemLongClickListener(onItemLongClick)
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

    private val onItemClick = OnItemClickListener { item, _ ->
        if (item is AskingFriendsItem) {
            if (Jajinternet.getStatusInternet(this)) {
                val inflater = LayoutInflater.from(this)
                val view = inflater.inflate(R.layout.accept_refuse_popup, null)
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
                val refuse = view.findViewById<Button>(R.id.button_refuse)
                val accept = view.findViewById<Button>(R.id.button_accept)
                val usernametext = view.findViewById<TextView>(R.id.addfriend_userName)
                val profilepicture = view.findViewById<ImageView>(R.id.addfriend_profile_picture)
                refuse.setOnClickListener {
                    popupWindow.dismiss()
                }

                accept.setOnClickListener {
                    popupWindow.dismiss()
                    FireStore.addFriends(item.user.uid)
                    askingFriendsSection.remove(item)
                }
                refuse.setOnClickListener {
                    popupWindow.dismiss()
                    FireStore.refuseFriend(item.user.uid)
                    askingFriendsSection.remove(item)
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
            else {
                Toast.makeText(this, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, AskingFriendsActivity::class.java)
            return intent
        }
    }
}