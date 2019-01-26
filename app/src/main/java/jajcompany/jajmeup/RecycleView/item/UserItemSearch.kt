package jajcompany.jajmeup.RecycleView.item


import android.content.Context
import android.content.res.Resources
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.models.User
import jajcompany.jajmeup.R
import jajcompany.jajmeup.utils.StorageUtil
import jajcompany.jajmeup.glide.GlideApp
import kotlinx.android.synthetic.main.community_list_item.*

class UserItemSearch(val user: User, val userId: String, private val context: Context): Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = user.name
        if (user.profilePicture != null) {
            GlideApp.with(context).load(StorageUtil.pathToReference(user.profilePicture))
                    .placeholder(R.drawable.ic_account_circle_black_24dp).into(viewHolder.imageView_profile_picture)
            Glide.with(context).load(StorageUtil.pathToReference(user.profilePicture)).apply(RequestOptions.circleCropTransform()).into(viewHolder.imageView_profile_picture)
        }
        if (user.reveilCurrentHour == "down") {
            viewHolder.textView_reveil.text = Resources.getSystem().getString(R.string.label_state_off_clock_list_user_string)
            viewHolder.textView_reveil.setTextColor(context.getColor(R.color.reveilDown))
        }
        else {
            viewHolder.textView_reveil.text = Resources.getSystem().getString(R.string.label_state_on_clock_list_user_string)//+user.reveilCurrentHour
            viewHolder.textView_reveil.setTextColor(context.getColor(R.color.reveilUp))
        }
        if(user.isFriend){
            viewHolder.isFriendSearch.text = Resources.getSystem().getString(R.string.hidden_isfriend_true_search_string)
        }

    }

    override fun getLayout() = R.layout.community_list_item
}