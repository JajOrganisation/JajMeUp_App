package jajcompany.jajmeup.fragment


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.R
import jajcompany.jajmeup.RecycleView.item.HistoryItem
import jajcompany.jajmeup.utils.FireStore
import jajcompany.jajmeup.utils.Jajinternet
import kotlinx.android.synthetic.main.history_layout.*

class HistoryFragment : Fragment() {

    private lateinit var historyListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var historySection: Section

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val receiveDeconnect = LocalBroadcastManager.getInstance(this@HistoryFragment.context!!)
        receiveDeconnect.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("HELLO", "signout receive community fragment")
                receiveDeconnect.unregisterReceiver(this)
                try {
                    FireStore.removeListener(historyListenerRegistration)
                } catch (e: Exception) {
                    Log.d("HELLO", "History unset "+e)
                }
            }
        }, IntentFilter("deconnectUser"))
        historyListenerRegistration = FireStore.addHistoryListener(this.activity!!, this::updateRecyclerView)
        return inflater?.inflate(R.layout.history_layout, container, false)
    }

    private fun updateRecyclerView(items:List<Item>) {
        fun init() {
            try {
                history_list.apply {
                    layoutManager = LinearLayoutManager(this@HistoryFragment.context)
                    adapter = GroupAdapter<ViewHolder>().apply {
                        historySection = Section(items)
                        add(historySection)
                        setOnItemClickListener(onItemClick)
                    }
                }
                shouldInitRecyclerView = false
            } catch (e: Exception) {
                Log.e("Error", "Error update history")
            }
        }
        fun updateItems() = historySection.update(items)

        if (shouldInitRecyclerView)
            init()
        else{
            try {
                updateItems()
            } catch (e: Exception) {
                Log.e("Error", "Error update history")
            }
        }

    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is HistoryItem) {
            if (Jajinternet.getStatusInternet(context)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.history.lien)))

            }
            else{
                Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }
    }
}