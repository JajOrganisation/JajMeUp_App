package jajcompany.jajmeup.activity

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_TASK_ON_HOME
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.models.History
import jajcompany.jajmeup.models.NotifWakeUp
import jajcompany.jajmeup.utils.FireStore
import jajcompany.jajmeup.utils.Jajinternet
import java.util.*

class LoadingAlarm : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            this.setTurnScreenOn(true)
        } else {
            val windoWw = window
            windoWw.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        Log.d("HELLO", "Dans le loading")
        if (Jajinternet.getStatusInternet(this)) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            if (sharedPreferences.getString("visibility_preference", "WORLD") != "PRIVATE") {
               FireStore.getLastReveil(this) {
                   if (it.videoname != "ERROR") {
                       if(it.votant != "Ton réveil") {
                           val user = FirebaseAuth.getInstance().currentUser
                           val notif = NotifWakeUp("Wakeup", it.lien, it.videoname, user!!.uid, Calendar.getInstance().time, "unread")
                           val history = History(it.lien, it.videoname, it.votantuid, Calendar.getInstance().time)
                           FireStore.sendNotifWakeUp(notif, it.votantuid)
                           FireStore.updateHistory(history)
                       }
                       else {
                           this.startActivity(YouTubeJAJActivity.newIntent(this, "Ton réveil", sharedPreferences.getString("default_reveil", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"), it.message))
                       }
                       this.startActivity(YouTubeJAJActivity.newIntent(this, it.votant, it.lien, it.message))
                   } else {
                       this.startActivity(LastAlarmActivity.newIntent(this))
                   }
               }

            }
            else {
                this.startActivity(YouTubeJAJActivity.newIntent(this, "Ton réveil", sharedPreferences.getString("default_reveil", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"), "Tu n'as pas voulu reçevoir de vote"))
            }
        }
        else {
             val intentt = LastAlarmActivity.newIntent(this)
             //intentt.addFlags(Intent.ACTION_OPEN_DOCUMENT)
            //intentt.flags = FLAG_ACTIVITY_TASK_ON_HOME
            this.startActivity(intentt)
        }
        val intentFinish = Intent()
        intentFinish.action = "finish_principal"
        intentFinish.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
        this.sendBroadcast(intentFinish)
        finish()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LoadingAlarm::class.java)
        }
    }
}