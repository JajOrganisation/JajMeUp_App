package jajcompany.jajmeup.Fragment

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.CommunityExpandableAdapter
import jajcompany.jajmeup.Utils.HistoryAdapter
import kotlinx.android.synthetic.main.community_layout.*
import kotlinx.android.synthetic.main.history_layout.*
import java.util.ArrayList

class HistoryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.history_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listHistory = ArrayList<HistoryAdapter.HitoryItem>()

        val item: HistoryAdapter.HitoryItem = HistoryAdapter.HitoryItem("Never Gonna...", R.drawable.abc_ic_star_black_36dp)

        listHistory.add(item)
        listHistory.add(item)

        val listAdapter = HistoryAdapter(this.activity!!, listHistory)

        history_list!!.adapter = listAdapter

    }
}