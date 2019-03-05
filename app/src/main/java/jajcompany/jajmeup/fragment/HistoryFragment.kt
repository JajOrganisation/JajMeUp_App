package jajcompany.jajmeup.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.R
import jajcompany.jajmeup.RecycleView.item.VoteItem
import jajcompany.jajmeup.utils.FireStore
import jajcompany.jajmeup.utils.Jajinternet
import kotlinx.android.synthetic.main.history_layout.*

class HistoryFragment : Fragment() {

    private lateinit var voteListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var voteSection: Section

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val receiveDeconnect = LocalBroadcastManager.getInstance(this@HistoryFragment.context!!)
        receiveDeconnect.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("HELLO", "signout receive community fragment")
                receiveDeconnect.unregisterReceiver(this)
                try {
                    FireStore.removeListener(voteListenerRegistration)
                } catch (e: Exception) {
                    Log.d("HELLO", "History unset "+e)
                }
            }
        }, IntentFilter("deconnectUser"))
        voteListenerRegistration = FireStore.addReveilListener(this.activity!!, this::updateRecyclerView)
        return inflater?.inflate(R.layout.history_layout, container, false)
    }

    private fun updateRecyclerView(items:List<Item>) {
        fun init() {
            try {
                history_list.apply {
                    layoutManager = LinearLayoutManager(this@HistoryFragment.context)
                    adapter = GroupAdapter<ViewHolder>().apply {
                        voteSection = Section(items)
                        add(voteSection)
                        setOnItemClickListener(onItemClick)
                    }
                }
                shouldInitRecyclerView = false
            } catch (e: Exception) {
                Log.e("Error", "Error update votes")
            }
        }
        fun updateItems() = voteSection.update(items)

        if (shouldInitRecyclerView)
            init()
        else{
            try {
                updateItems()
            } catch (e: Exception) {
                Log.e("Error", "Error update votes")
            }
        }

    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is VoteItem) {
            if (Jajinternet.getStatusInternet(context)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+item.vote.lien)))

            }
            else{
                Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }
    }
}