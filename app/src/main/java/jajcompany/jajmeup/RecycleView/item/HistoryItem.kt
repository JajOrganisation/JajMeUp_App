package jajcompany.jajmeup.RecycleView.item

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.R
import jajcompany.jajmeup.glide.GlideApp
import jajcompany.jajmeup.models.History
import jajcompany.jajmeup.utils.StorageUtil
import kotlinx.android.synthetic.main.history_list_item.*

class HistoryItem(val history: History, val username: String, val profilpicture: String, private val context: Context): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.usernameHistory.text= username
        viewHolder.videoNameHistory.text = history.videoname
        if (profilpicture != null) {
            GlideApp.with(context).load(StorageUtil.pathToReference(profilpicture))
                    .placeholder(R.drawable.ic_account_circle_black_24dp).into(viewHolder.userPictureHistory)
            Glide.with(context).load(StorageUtil.pathToReference(profilpicture)).apply(RequestOptions.circleCropTransform()).into(viewHolder.userPictureHistory)
        }
    }

    override fun getLayout() = R.layout.history_list_item
}