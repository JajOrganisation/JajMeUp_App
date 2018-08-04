package jajcompany.jajmeup.Utils

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import jajcompany.jajmeup.R

class CommunityExpandableAdapter(val context: Context, val listHeader: List<String>, val listChild: HashMap<String, MutableList<itemCommunauty>>) : BaseExpandableListAdapter() {
    override fun getGroup(groupPosition: Int): Any {
        return listHeader[groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val headerValue = getGroup(groupPosition) as String
        val view: View = LayoutInflater.from(context).inflate(R.layout.community_list_header, parent, false)
        val listHeaderTxt = view.findViewById<TextView>(R.id.header_communauty) as TextView

        listHeaderTxt.setTypeface(null, Typeface.BOLD)
        listHeaderTxt.text = headerValue

        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return listChild[listHeader[groupPosition]]!!.size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return listChild[listHeader[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val child = getChild(groupPosition, childPosition) as CommunityExpandableAdapter.itemCommunauty
        val view: View = LayoutInflater.from(context).inflate(R.layout.community_list_item, parent, false)
        val listItemTxt = view.findViewById<TextView>(R.id.textView_name)
        val listItemImg = view.findViewById<ImageView>(R.id.imageView_profile_picture)

        listItemImg.setImageResource(child.profilPicture)
        listItemTxt.text = child.userName


        return view
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return listHeader.size
    }

    data class itemCommunauty(val userName: String, val profilPicture: Int)
}