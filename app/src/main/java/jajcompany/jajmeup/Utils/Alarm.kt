package jajcompany.jajmeup.Utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Switch
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.Activity.YouTubeJAJActivity
import jajcompany.jajmeup.Models.NotifWakeUp
import jajcompany.jajmeup.Utils.FireStore.sendNotifWakeUp
import java.util.*

@SuppressLint("StaticFieldLeak")
object Alarm {
    lateinit var alarmManager: AlarmManager
    lateinit var switchAlarm: Switch
    lateinit var onReveilInfoReceiver: onReveilInfo

    fun setAlarm(context: Context, hours: Int, minutes: Int, switchA: Switch) {
        val intent = Intent(context, onAlarm::class.java)
        intent.action = "onReveilRing"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        switchAlarm = switchA
        val filter = IntentFilter()
        filter.addAction("onReveilINFO")
        onReveilInfoReceiver = onReveilInfo()
        context.registerReceiver(onReveilInfoReceiver, filter)
    }

    fun deleteAlarm(context: Context) {
        val intent = Intent(context, onAlarm::class.java)
        intent.action = "onReveilRing"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
        context.unregisterReceiver(onReveilInfoReceiver)
    }

    class onAlarm : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action == "onReveilRing") {
                Toast.makeText(context, "Ca sonne mon gars", Toast.LENGTH_SHORT).show()
                FireStore.getLastReveil(context)
            }
        }
    }

    class onReveilInfo : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action == "onReveilINFO") {
                val user = FirebaseAuth.getInstance().currentUser
                val notif = NotifWakeUp("Wakeup", intent?.getStringExtra("lien"), YoutubeInformation.getTitleQuietly(intent?.getStringExtra("lien")), user!!.uid, Calendar.getInstance().time)
                sendNotifWakeUp(notif, intent?.getStringExtra("votantuid"))
                val intent = YouTubeJAJActivity.newIntent(context, intent?.getStringExtra("votant"), intent?.getStringExtra("lien"), intent?.getStringExtra("message"))
                switchAlarm.setChecked(false)
                context.startActivity(intent)
            }
        }
    }
}