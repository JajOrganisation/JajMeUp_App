package jajcompany.jajmeup.Fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.CommunityExpandableAdapter
import kotlinx.android.synthetic.main.community_layout.*

class CommunityFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.community_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listFriend = mutableListOf<CommunityExpandableAdapter.itemCommunauty>()
        val tmp: CommunityExpandableAdapter.itemCommunauty = CommunityExpandableAdapter.itemCommunauty("BOB", R.drawable.abc_ic_star_black_36dp)

        listFriend.add(tmp)

        val listWorld = mutableListOf<CommunityExpandableAdapter.itemCommunauty>()
        val tmp2: CommunityExpandableAdapter.itemCommunauty = CommunityExpandableAdapter.itemCommunauty("ALICE", R.drawable.abc_ic_star_half_black_36dp)

        listWorld.add(tmp2)

        val listHeader = listOf("Mes amis", "Tout le monde")
        val listChild = HashMap<String, MutableList<CommunityExpandableAdapter.itemCommunauty>>()

        listChild.put(listHeader[0], listFriend)
        listChild.put(listHeader[1], listWorld)

        val expandableAdapter = CommunityExpandableAdapter(activity, listHeader, listChild)

        community_list.setAdapter(expandableAdapter)
    }
}