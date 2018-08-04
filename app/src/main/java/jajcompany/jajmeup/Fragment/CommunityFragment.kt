package jajcompany.jajmeup.Fragment

import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.R
import jajcompany.jajmeup.R.attr.layoutManager
import jajcompany.jajmeup.RecycleView.item.UserItem
import jajcompany.jajmeup.Utils.CommunityExpandableAdapter
import jajcompany.jajmeup.Utils.FireStore
import kotlinx.android.synthetic.main.community_layout.*

class CommunityFragment : Fragment() {

    lateinit var databaseRef: DatabaseReference
    private lateinit var userListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var userSection: Section

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        userListenerRegistration = FireStore.addUsersListener(this.activity!!, this::updateRecyclerView)
        return inflater?.inflate(R.layout.community_layout, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FireStore.removeListener(userListenerRegistration)
        shouldInitRecyclerView = true
    }

    private fun updateRecyclerView(items:List<Item>) {
        fun init() {
            community_list.apply {
                layoutManager = LinearLayoutManager(this@CommunityFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    userSection = Section(items)
                    add(userSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }
        fun updateItems() = userSection.update(items)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()
    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is UserItem) {
            Toast.makeText(activity, "Click on User", Toast.LENGTH_LONG).show()
        }
    }

   /* override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
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

        community_list.setOnChildClickListener { expandableListView, view, groupPosition, childPosition, l ->
            if (arguments != null) {
                Toast.makeText(activity, ""+listChild[listHeader[groupPosition]]!![childPosition].userName + " va avoir ce reveil : " + arguments.getString("link"), Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(
                        activity,
                        " Aucun lien pour "
                                + listChild[listHeader[groupPosition]]!![childPosition].userName,
                        Toast.LENGTH_LONG).show()
            }
            false}
        databaseRef = FirebaseDatabase.getInstance().getReference()
        getGreeting()

    }*/

    /* override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         if (arguments != null) {
             Log.d("YOUTUBE_FRAGMENT", arguments.getString("link"))
         }
    }*/

    companion object {

        fun newInstance(link: String): CommunityFragment {
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