package jajcompany.jajmeup.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.FireStore
import kotlinx.android.synthetic.main.askingfriends_layout.*
import kotlinx.android.synthetic.main.community_layout.*

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
                   /* setOnItemClickListener(onItemClick)
                    setOnItemLongClickListener(onItemLongClick)*/
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
    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, AskingFriendsActivity::class.java)
            return intent
        }
    }
}