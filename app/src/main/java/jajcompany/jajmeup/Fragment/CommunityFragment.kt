package jajcompany.jajmeup.Fragment

import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import jajcompany.jajmeup.Utils.YoutubeInformation
import kotlinx.android.synthetic.main.vote_popup_layout.*
import java.util.*
import java.util.regex.Pattern


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
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.vote_popup_layout,null)
            val popupWindow = PopupWindow(
                    view, // Custom view to show in popup window
                    LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                    LinearLayout.LayoutParams.WRAP_CONTENT // Window height
            )

            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut
            popupWindow.isFocusable = true
            val closepop = view.findViewById<Button>(R.id.button_closepop)
            val votepop = view.findViewById<Button>(R.id.button_votepop)
            val edityt = view.findViewById<EditText>(R.id.youtubelinkpop)
            val editmess = view.findViewById<EditText>(R.id.messagepop)
            val labelmess = view.findViewById<TextView>(R.id.message_label)
            labelmess.setText("Écrit un message pour "+item.user.name)
            closepop.setOnClickListener{
                popupWindow.dismiss()
            }

            votepop.setOnClickListener {
                val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
                val compiledPattern = Pattern.compile(pattern)
                val matcher = compiledPattern.matcher(edityt.text.toString())
                if (matcher.find()) {
                    val user = FirebaseAuth.getInstance().currentUser
                    val vote = Vote(matcher.group(), YoutubeInformation.getTitleQuietly(matcher.group()), user?.displayName.toString(), editmess.text.toString(), Calendar.getInstance().time)
                    FireStore.sendVote(vote, item.userId)
                    popupWindow.dismiss()
                    Toast.makeText(activity,"Tu as voté pour "+item.user.name, Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(activity, "Invalid link", Toast.LENGTH_LONG).show()
                }
            }
            if (arguments?.getString("link") != null){
                val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
                val compiledPattern = Pattern.compile(pattern)
                val matcher = compiledPattern.matcher(arguments?.getString("link").toString())
                if (matcher.find()) {
                    edityt.setText(arguments?.getString("link").toString())

                }
            }
            TransitionManager.beginDelayedTransition(community_layout)
            popupWindow.showAtLocation(
                    community_layout,
                    Gravity.CENTER,
                    0,
                    0
            )
           /* if (arguments?.getString("link") != null){
                val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
                val compiledPattern = Pattern.compile(pattern)
                val matcher = compiledPattern.matcher(arguments?.getString("link").toString())
                if (matcher.find()) {
                    Toast.makeText(activity, "Give "+matcher.group()+"for "+item.user.name, Toast.LENGTH_LONG).show()
                    val user = FirebaseAuth.getInstance().currentUser
                    val vote = Vote(matcher.group(), user?.displayName.toString(), "bonjour", Calendar.getInstance().time)
                    FireStore.sendVote(vote, item.userId)
                }
                else
                {
                    Toast.makeText(activity, "Invalid link", Toast.LENGTH_LONG).show()
                }
            }
            else {
                Toast.makeText(activity, "Click on User", Toast.LENGTH_LONG).show()
            }*/

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