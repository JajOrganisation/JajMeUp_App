package jajcompany.jajmeup.activity

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_TASK_ON_HOME
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.models.NotifWakeUp
import jajcompany.jajmeup.utils.FireStore
import jajcompany.jajmeup.utils.Jajinternet
import java.util.*

class LoadingAlarm : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Jajinternet.getStatusInternet(this)) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            if (sharedPreferences.getString("visibility_preference", "WORLD") != "PRIVATE") {
               FireStore.getLastReveil(this) {
                   if (it.videoname != "ERROR") {
                       val user = FirebaseAuth.getInstance().currentUser
                       val notif = NotifWakeUp("Wakeup", it.lien, it.videoname, user!!.uid, Calendar.getInstance().time, "unread")
                       FireStore.sendNotifWakeUp(notif, it.votantuid)
                       val intenttt = YouTubeJAJActivity.newIntent(this, it.votant, it.lien, it.message)
                       this.startActivity(intenttt)
                   } else {
                       val intentttt = LastAlarmActivity.newIntent(this)
                       this.startActivity(intentttt)
                   }
               }

            }
            else {
                val intenttttt = YouTubeJAJActivity.newIntent(this, "Ton réveil", sharedPreferences.getString("default_reveil", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"), "Tu n'as pas voulu reçevoir de vote")
                this.startActivity(intenttttt)
            }
        }
        else {
             val intentt = LastAlarmActivity.newIntent(this)
             //intentt.addFlags(Intent.ACTION_OPEN_DOCUMENT)
            intentt.flags = FLAG_ACTIVITY_TASK_ON_HOME
            this.startActivity(intentt)
        }
        finish()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, LoadingAlarm::class.java)
            return intent
        }
    }
}