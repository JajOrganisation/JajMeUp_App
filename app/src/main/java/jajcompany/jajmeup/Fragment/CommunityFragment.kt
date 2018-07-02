package jajcompany.jajmeup.Fragment

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.*
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.CommunityExpandableAdapter
import kotlinx.android.synthetic.main.community_layout.*

class CommunityFragment : Fragment() {

    lateinit var databaseRef: DatabaseReference


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

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            Log.d("YOUTUBE_FRAGMENT", arguments.getString("link"))
        }
    }

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