package jajcompany.jajmeup.RecycleView.item

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.Models.User
import jajcompany.jajmeup.Models.Vote
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.StorageUtil
import jajcompany.jajmeup.glide.GlideApp
import kotlinx.android.synthetic.main.community_list_item.*
import kotlinx.android.synthetic.main.history_list_item.*

class VoteItem(val vote: Vote, val user: User, private val context: Context): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.usernameHistory.text= user.name
        viewHolder.videoNameHistory.text = vote.videoname
        if (user.profilePicture != null) {
            GlideApp.with(context).load(StorageUtil.pathToReference(user.profilePicture))
                    .placeholder(R.drawable.ic_account_circle_black_24dp).into(viewHolder.userPictureHistory)
            Glide.with(context).load(StorageUtil.pathToReference(user.profilePicture)).apply(RequestOptions.circleCropTransform()).into(viewHolder.userPictureHistory)
        }
    }

    override fun getLayout() = R.layout.history_list_item
}