package jajcompany.jajmeup.Utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Switch
import android.widget.Toast
import jajcompany.jajmeup.Activity.YouTubeJAJActivity
import jajcompany.jajmeup.R.id.alarmSet
import java.util.*

@SuppressLint("StaticFieldLeak")
object Alarm {
    lateinit var alarmManager: AlarmManager
    lateinit var switchAlarm: Switch

    fun setAlarm(context: Context, hours: Int, minutes: Int, switchA: Switch) {
        val intent = Intent(context, onAlarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        switchAlarm = switchA
    }

    fun deleteAlarm(context: Context) {
        val intent = Intent(context, onAlarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

    class onAlarm : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            Toast.makeText(context, "Ca sonne mon gars", Toast.LENGTH_SHORT).show()
            switchAlarm.setChecked(false)
            context.startActivity(YouTubeJAJActivity.newIntent(context))
        }
    }
}