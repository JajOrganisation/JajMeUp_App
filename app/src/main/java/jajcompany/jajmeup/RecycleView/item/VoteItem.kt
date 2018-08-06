package jajcompany.jajmeup.RecycleView.item

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.Models.Vote
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.history_list_item.*

class VoteItem(val vote: Vote, val userId: String, private val context: Context): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val auth = FirebaseAuth.getInstance()
        viewHolder.videoName.text = vote.lien

    }

    override fun getLayout() = R.layout.history_list_item
}