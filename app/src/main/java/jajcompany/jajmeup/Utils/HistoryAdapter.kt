package jajcompany.jajmeup.Utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import jajcompany.jajmeup.R
import java.util.ArrayList

class HistoryAdapter(private var context: Context, private var voteYoutube: ArrayList<HitoryItem>): BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()
            val inflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.history_list_item, null, true)

            holder.videoName = convertView!!.findViewById(R.id.videoName) as TextView
            holder.userPhoto = convertView.findViewById(R.id.userPictureHistory) as ImageView

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.videoName!!.setText(voteYoutube[position].videoName)
        holder.userPhoto!!.setImageResource(voteYoutube[position].profilPicture)

        return convertView
    }

    private inner class ViewHolder {

        var videoName: TextView? = null
        internal var userPhoto: ImageView? = null

    }

    override fun getItem(position: Int): Any {
        return voteYoutube[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return voteYoutube.count()
    }

    data class HitoryItem(val videoName: String, val profilPicture: Int)

}