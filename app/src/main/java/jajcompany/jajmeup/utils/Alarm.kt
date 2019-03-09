package jajcompany.jajmeup.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.activity.YouTubeJAJActivity
import jajcompany.jajmeup.activity.LastAlarmActivity
import jajcompany.jajmeup.models.NotifWakeUp
import jajcompany.jajmeup.utils.FireStore.sendNotifWakeUp
import java.util.*


@SuppressLint("StaticFieldLeak")
object Alarm {
    lateinit var alarmManager: AlarmManager
    lateinit var switchAlarm: Switch
    lateinit var onReveilInfoReceiver: OnReveilInfo

    fun setAlarm(context: Context, hours: Int, minutes: Int, switchA: Switch) {
        val intent = Intent(context, OnAlarm::class.java)
        intent.action = "onReveilRing"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, 0)
        Log.d("HELLO", "HEURE "+cal.timeInMillis)
        var time = cal.timeInMillis - cal.timeInMillis % 60000
        if (System.currentTimeMillis() > time) {
            if (Calendar.AM_PM === 0)
                time += 1000 * 60 * 60 * 12
            else
                time += time + 1000 * 60 * 60 * 24
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        switchAlarm = switchA
        val filter = IntentFilter()
        filter.addAction("onReveilINFO")
        onReveilInfoReceiver = OnReveilInfo()
        context.registerReceiver(onReveilInfoReceiver, filter)
    }

    fun deleteAlarm(context: Context) {
        val intent = Intent(context, OnAlarm::class.java)
        intent.action = "onReveilRing"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
        context.unregisterReceiver(onReveilInfoReceiver)
    }

    class OnAlarm : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action == "onReveilRing") {
                Toast.makeText(context, "Ca sonne mon gars", Toast.LENGTH_SHORT).show()
                if (Jajinternet.getStatusInternet(context)) {
                    FireStore.getLastReveil(context)
                }
                else {
                    val intentt = LastAlarmActivity.newIntent(context)
                    //intentt.addFlags(Intent.ACTION_OPEN_DOCUMENT)
                    switchAlarm.isChecked = false
                    context.startActivity(intentt)
                }
            }
        }
    }

    class OnReveilInfo : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action == "onReveilINFO") {
                switchAlarm.isChecked = false
                if (intent.getStringExtra("votant") != "Ton réveil") {
                    val titlevideo = YoutubeInformation.getTitleQuietly(YoutubeInformation.getIDFromURL(intent.getStringExtra("lien")))
                    if (titlevideo != "ERROR") {
                        val user = FirebaseAuth.getInstance().currentUser
                        val notif = NotifWakeUp("Wakeup", intent.getStringExtra("lien"), YoutubeInformation.getTitleQuietly(YoutubeInformation.getIDFromURL(intent.getStringExtra("lien"))), user!!.uid, Calendar.getInstance().time, "unread")
                        sendNotifWakeUp(notif, intent.getStringExtra("votantuid"))
                    } else {
                        val intentt = LastAlarmActivity.newIntent(context)
                        context.startActivity(intentt)
                    }
                }
                val titlevideo = YoutubeInformation.getTitleQuietly(YoutubeInformation.getIDFromURL(intent.getStringExtra("lien")))
                if (titlevideo != "ERROR") {
                    val intentt = YouTubeJAJActivity.newIntent(context, intent.getStringExtra("votant"), intent.getStringExtra("lien"), intent.getStringExtra("message"))
                    context.startActivity(intentt)
                } else {
                    val intentt = LastAlarmActivity.newIntent(context)
                    context.startActivity(intentt)
                }
            }
        }
    }
}