package jajcompany.jajmeup.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import java.util.*
import jajcompany.jajmeup.activity.*
import java.lang.Exception
import java.text.DateFormat


@SuppressLint("StaticFieldLeak")
object Alarm {
    lateinit var alarmManagerPrincipal: AlarmManager
    lateinit var alarmManagerBetween: AlarmManager
    lateinit var switchAlarm: Switch
    //lateinit var onReveilInfoReceiver: OnReveilInfo

    fun setAlarm(context: Context, hours: Int, minutes: Int, switchA: Switch) {
        val intentAlarmPrincipale = Intent(context, OnAlarm::class.java)
        intentAlarmPrincipale.action = "onReveilRing"
        val pendingAlarmPrincipal = PendingIntent.getBroadcast(context, 0, intentAlarmPrincipale, PendingIntent.FLAG_UPDATE_CURRENT)
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
        alarmManagerPrincipal = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManagerPrincipal.setExact(AlarmManager.RTC_WAKEUP, time, pendingAlarmPrincipal)
        switchAlarm = switchA


        val intentAlarmBetween = Intent(context, OnUpdateBetween::class.java)
        intentAlarmBetween.action = "onUpdateTimer"
        val pendingAlarmBetween = PendingIntent.getBroadcast(context, 0, intentAlarmBetween, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManagerBetween = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManagerBetween.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), 1000, pendingAlarmBetween)
        FireStore.updateCurrentUser(reveilCurrent =  "up")
    }

    fun deleteAlarm(context: Context) {
        try {
            val intentPrincipal = Intent(context, OnAlarm::class.java)
            intentPrincipal.action = "onReveilRing"
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intentPrincipal, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManagerPrincipal.cancel(pendingIntent)
            val intentBetween = Intent(context, OnAlarm::class.java)
            intentBetween.action = "onUpdateTimer"
            val pendingIntentBetween = PendingIntent.getBroadcast(context, 0, intentBetween, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManagerBetween.cancel(pendingIntentBetween)
            //context.unregisterReceiver(onReveilInfoReceiver)
            FireStore.updateCurrentUser(reveilCurrent = "down")
        }catch (e: Exception) {

        }
    }

    fun getBetween(alarmTotalDepart: String): String{
        val getTime = Calendar.getInstance()
        Log.d("HELLO", "AM PM "+getTime.get(Calendar.AM_PM).toString())
        val alarmTotal = (alarmTotalDepart.split(':')[0].toInt()).toString()+":"+ (alarmTotalDepart.split(':')[1].toInt()).toString()
        var currentTimeTotal = (getTime.get(Calendar.HOUR)).toString()+":"+getTime.get(Calendar.MINUTE).toString()
        if (getTime.get(Calendar.AM_PM) == 1)
            currentTimeTotal = ((getTime.get(Calendar.HOUR))+12).toString()+":"+getTime.get(Calendar.MINUTE).toString()
        Log.d("HELLO", "TIME = Current $currentTimeTotal Alarm $alarmTotal")
        var hoursfinal = 0
        var minutesfinal = 0
        if (currentTimeTotal.split(':')[0].toInt() > alarmTotal!!.split(':')[0].toInt())
            hoursfinal = (23-currentTimeTotal.split(':')[0].toInt())+alarmTotal.split(':')[0].toInt()
        else
            hoursfinal = (alarmTotal.split(':')[0].toInt()-currentTimeTotal.split(':')[0].toInt())-1
        if (currentTimeTotal.split(':')[1].toInt() > alarmTotal.split(':')[1].toInt())
            minutesfinal = (60-currentTimeTotal.split(':')[1].toInt())+alarmTotal.split(':')[1].toInt()
        else{
            hoursfinal += 1
            minutesfinal = alarmTotal.split(':')[1].toInt()-currentTimeTotal.split(':')[1].toInt()
        }
        var hoursfinalString = hoursfinal.toString()
        var minutesfinalString = minutesfinal.toString()
        if (hoursfinal < 10)
            hoursfinalString = "0$hoursfinal"
        if (minutesfinal < 10)
            minutesfinalString = "0$minutesfinal"
        Log.d("HELLO", "TIME FINAL = $hoursfinalString $minutesfinalString")
        return "$hoursfinalString:$minutesfinalString"
    }

    class OnAlarm : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action == "onReveilRing") {
                Log.d("HELLO", "On sonne")
                Alarm.deleteAlarm(context)
                //switchAlarm.isChecked = false
                Toast.makeText(context, "Ca sonne mon gars", Toast.LENGTH_LONG).show()
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                if (sharedPreferences.getString("hours_clock", "-11:-11") != "-11:-11") {
                    val haha = LoadingAlarm.newIntent(context)
                    haha.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(haha)
                }
            }
        }
    }

    class OnUpdateBetween : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action == "onUpdateTimer") {
                Log.d("HELLO", "On change le between time")
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPreferences.edit().putString("between_time", Alarm.getBetween(sharedPreferences.getString("hours_clock", "-11:-11")!!.toString())).apply()
            }
        }
    }
}