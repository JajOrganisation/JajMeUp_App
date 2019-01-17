package jajcompany.jajmeup.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.R
import jajcompany.jajmeup.utils.FireStore
import kotlinx.android.synthetic.main.history_layout.*

class HistoryFragment : Fragment() {

    private lateinit var voteListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var voteSection: Section

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
}