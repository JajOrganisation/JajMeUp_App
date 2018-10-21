package jajcompany.jajmeup.RecycleView.item


import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.glide.GlideApp
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.Models.User
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.StorageUtil
import kotlinx.android.synthetic.main.community_list_item.*
import kotlinx.android.synthetic.main.profilepicturesettings_layout.*

class UserItem(val user: User, val userId: String, private val context: Context): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = user.name
        if (user.profilePicture != null) {
            GlideApp.with(context).load(StorageUtil.pathToReference(user.profilePicture))
                    .placeholder(R.drawable.ic_account_circle_black_24dp).into(viewHolder.imageView_profile_picture)
            Glide.with(context).load(StorageUtil.pathToReference(user.profilePicture)).apply(RequestOptions.circleCropTransform()).into(viewHolder.imageView_profile_picture)
        }
        if (user.reveilCurrentHour == "down") {
            viewHolder.textView_reveil.text = "N'a pas mis de réveil"
            viewHolder.textView_reveil.setTextColor(context.getColor(R.color.reveilDown))
        }
        else {
            viewHolder.textView_reveil.text = "Se réveil à "+user.reveilCurrentHour
            viewHolder.textView_reveil.setTextColor(context.getColor(R.color.reveilUp))
        }

    }

    override fun getLayout() = R.layout.community_list_item
}