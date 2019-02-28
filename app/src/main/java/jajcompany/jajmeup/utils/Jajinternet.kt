package jajcompany.jajmeup.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object Jajinternet {
    fun getStatusInternet(context: Context?): Boolean {
        val connmanag = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeCheck: NetworkInfo? = connmanag.activeNetworkInfo
        if (activeCheck != null && activeCheck.isConnected)
            return true
        return false
    }
}