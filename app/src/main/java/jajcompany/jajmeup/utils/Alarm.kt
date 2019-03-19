package jajcompany.jajmeup.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import java.util.*
import jajcompany.jajmeup.activity.*
import java.lang.Exception


@SuppressLint("StaticFieldLeak")
object Alarm {
    lateinit var alarmManager: AlarmManager
    lateinit var switchAlarm: Switch
    //lateinit var onReveilInfoReceiver: OnReveilInfo

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
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        switchAlarm = switchA
        FireStore.updateCurrentUser(reveilCurrent =  "up")
    }

    fun deleteAlarm(context: Context) {
        try {
            val intent = Intent(context, OnAlarm::class.java)
            intent.action = "onReveilRing"
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            //context.unregisterReceiver(onReveilInfoReceiver)
            FireStore.updateCurrentUser(reveilCurrent = "down")
        }catch (e: Exception) {

        }
    }

    class OnAlarm : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action == "onReveilRing") {
                val intent = Intent()
                intent.action = "finish_principal"
                intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                context.sendBroadcast(intent)
                //switchAlarm.isChecked = false
                Toast.makeText(context, "Ca sonne mon gars", Toast.LENGTH_LONG).show()
                val haha = LoadingAlarm.newIntent(context)
                haha.flags = Intent.FLAG_ACTIVITY_TASK_ON_HOME
                context.startActivity(haha)
            }
        }
    }
}