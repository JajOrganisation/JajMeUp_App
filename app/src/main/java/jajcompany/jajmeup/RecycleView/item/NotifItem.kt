package jajcompany.jajmeup.RecycleView.item

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import jajcompany.jajmeup.Models.NotifWakeUp
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.StorageUtil
import jajcompany.jajmeup.glide.GlideApp
import kotlinx.android.synthetic.main.notification_item.*

class NotifItem(val notif: NotifWakeUp, val username: String, val profilePicture: String, private val context: Context): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        if (profilePicture != null) {
            GlideApp.with(context).load(StorageUtil.pathToReference(profilePicture))
                    .placeholder(R.drawable.ic_account_circle_black_24dp).into(viewHolder.userPictureNotif)
            Glide.with(context).load(StorageUtil.pathToReference(profilePicture)).apply(RequestOptions.circleCropTransform()).into(viewHolder.userPictureNotif)
        }
        if (notif.notificationtype == "Wakeup") {
            viewHolder.usernameNotif.text = "Tu as réveillé "+username+" avec la vidéo "+notif.videoname
            //viewHolder.videoNameNotif.text = "avec                                    "+notif.videoname
        }
    }

    override fun getLayout() = R.layout.notification_item
}