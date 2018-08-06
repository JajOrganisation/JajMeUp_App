package jajcompany.jajmeup.Fragment

import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.Models.Vote
import jajcompany.jajmeup.R
import jajcompany.jajmeup.R.attr.layoutManager
import jajcompany.jajmeup.RecycleView.item.UserItem
import jajcompany.jajmeup.Utils.CommunityExpandableAdapter
import jajcompany.jajmeup.Utils.FireStore
import kotlinx.android.synthetic.main.community_layout.*
import com.google.firebase.auth.FirebaseUser



class CommunityFragment : Fragment() {

    lateinit var databaseRef: DatabaseReference
    private lateinit var userListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var userSection: Section

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        userListenerRegistration = FireStore.addUsersListener(this.activity!!, this::updateRecyclerView)
       /* if (arguments != null) {
            Log.d("YOUTUBE_FRAGMENT", arguments.getString("link"))
        }*/
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
            if (arguments?.getString("link") != null){
                Toast.makeText(activity, "Give "+arguments?.getString("link")+"for "+item.user.name, Toast.LENGTH_LONG).show()
                val user = FirebaseAuth.getInstance().currentUser
                val vote = Vote(arguments?.getString("link").toString(), user?.displayName.toString())
                FireStore.sendVote(vote, item.userId)
            }
            else {
                Toast.makeText(activity, "Click on User", Toast.LENGTH_LONG).show()
            }

        }
    }

     companion object {

        fun newInstance(link: String): Fragment {
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