package jajcompany.jajmeup.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.FireStore
import kotlinx.android.synthetic.main.notification_layout.*

class NotificationActivity : AppCompatActivity() {

    private lateinit var notificationRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var notificationSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        notificationRegistration = FireStore.addNotificationListener(this, this::updateRecyclerView)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_layout)
    }

    private fun updateRecyclerView(items:List<Item>) {
        fun init() {
            notification_list.apply {
                layoutManager = LinearLayoutManager(this@NotificationActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    notificationSection = Section(items)
                    add(notificationSection)
                }
            }
            shouldInitRecyclerView = false
        }
        fun updateItems() = notificationSection.update(items)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, NotificationActivity::class.java)
            return intent
        }
    }
}