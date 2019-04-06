package jajcompany.jajmeup.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.preference.PreferenceManager
import android.provider.SyncStateContract
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.content.WakefulBroadcastReceiver
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import com.google.android.gms.common.internal.Constants
import jajcompany.jajmeup.R
import java.util.*
import jajcompany.jajmeup.activity.*
import java.lang.Exception
import java.sql.Time
import java.text.DateFormat
import kotlin.reflect.jvm.internal.impl.load.java.Constant


@SuppressLint("StaticFieldLeak")
object Alarm {
    lateinit var alarmManagerPrincipal: AlarmManager
    lateinit var alarmManagerBetween: AlarmManager
    lateinit var switchAlarm: Switch
    //lateinit var onReveilInfoReceiver: OnReveilInfo

    fun setAlarm(hours: Int, minutes: Int, switchA: Switch) {
        val contextApp = PrincipalActivity.applicationContext()
        val intentAlarmPrincipale = Intent(contextApp, OnAlarm::class.java)
        intentAlarmPrincipale.action = "onReveilRing"
        val pendingAlarmPrincipal = PendingIntent.getBroadcast(contextApp, 0, intentAlarmPrincipale, PendingIntent.FLAG_UPDATE_CURRENT)
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, 0)
        Log.d("HELLO", "HEURE "+cal.timeInMillis)
        var time = cal.timeInMillis - cal.timeInMillis % 60000

        if (System.currentTimeMillis() > time) {
            time += 1000 * 60 * 60 * 24
        }
        Log.d("HELLO", "TIME "+time.toString())
        alarmManagerPrincipal = contextApp.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //val ac = AlarmManager.AlarmClockInfo(time, pendingAlarmPrincipal)
        //alarmManagerPrincipal.setAlarmClock(ac, pendingAlarmPrincipal)
        alarmManagerPrincipal.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingAlarmPrincipal)
        switchAlarm = switchA
        setNotif(hours, minutes)
        val intentAlarmBetween = Intent(contextApp, OnUpdateBetween::class.java)
        intentAlarmBetween.action = "onUpdateTimer"
        val pendingAlarmBetween = PendingIntent.getBroadcast(contextApp, 0, intentAlarmBetween, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManagerBetween = contextApp.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManagerBetween.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), 1000*60, pendingAlarmBetween)
        FireStore.updateCurrentUser(reveilCurrent =  "up")
    }

    fun setNotif(hours: Int, minutes: Int) {
        val contextApp = PrincipalActivity.applicationContext()
        var hoursString = hours.toString()
        var minutesString = minutes.toString()
        if (hours < 10)
            hoursString = "0"+hoursString
        if (minutes < 10)
            minutesString = "0"+minutesString
        val testMachin = Intent(contextApp, AlarmNotificationService::class.java)
        testMachin.putExtra("heureReveil", "$hoursString:$minutesString")
        testMachin.putExtra("heureBetween", Alarm.getBetween("$hoursString:$minutesString"))
        ContextCompat.startForegroundService(contextApp, testMachin)
    }

    fun unsetNotif() {
        val contextApp = PrincipalActivity.applicationContext()
        val testMachin = Intent(contextApp, AlarmNotificationService::class.java)
        contextApp.stopService(testMachin)
    }

    fun deleteAlarm() {
        try {
            val contextApp = PrincipalActivity.applicationContext()
            val intentPrincipal = Intent(contextApp, OnAlarm::class.java)
            intentPrincipal.action = "onReveilRing"
            val pendingIntent = PendingIntent.getBroadcast(contextApp, 0, intentPrincipal, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManagerPrincipal = contextApp.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManagerPrincipal.cancel(pendingIntent)
            pendingIntent.cancel()
            val intentBetween = Intent(contextApp, OnUpdateBetween::class.java)
            intentBetween.action = "onUpdateTimer"
            val pendingIntentBetween = PendingIntent.getBroadcast(contextApp, 0, intentBetween, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManagerBetween = contextApp.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManagerBetween.cancel(pendingIntentBetween)
            pendingIntentBetween.cancel()
            //context.unregisterReceiver(onReveilInfoReceiver)
            unsetNotif()
            FireStore.updateCurrentUser(reveilCurrent = "down")
        }catch (e: Exception) {

        }
    }

    fun getBetween(alarmTotalDepart: String): String {
        val calStart: Calendar = Calendar.getInstance()
        calStart.set(Calendar.HOUR_OF_DAY, alarmTotalDepart.split(':')[0].toInt())
        calStart.set(Calendar.MINUTE, alarmTotalDepart.split(':')[1].toInt())
        calStart.set(Calendar.SECOND, 0)
        val calCurrentTime = Calendar.getInstance()

        var milliStart = calStart.timeInMillis
        val milliCurrent = calCurrentTime.timeInMillis

        if (milliStart < milliCurrent)
            milliStart += 1000 * 60 * 60 * 24

        Log.d("HELLO", "Start "+milliStart.toString()+" Current "+milliCurrent.toString())

        val diff = milliStart - milliCurrent

        Log.d("HELLO", "Diff "+diff.toString()+" "+((diff / (1000*60*60))%24).toString()+":"+((diff / (1000*60))%60).toString())

        return String.format("%02d", (diff / (1000*60*60))%24)+":"+ String.format("%02d", (diff / (1000*60))%60)
    }

    /*class OnAlarm : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action == "onReveilRing") {
                Log.d("HELLO", "On sonne")
                Alarm.deleteAlarm()
                //switchAlarm.isChecked = false
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                if (sharedPreferences.getString("hours_clock", "-11:-11") != "-11:-11") {
                    val haha = LoadingAlarm.newIntent(context)
                    haha.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(haha)
                }
                else {
                    Log.d("HELLO", "Recu alarm sans reveil")
                    deleteAlarm()
                }
            }
        }
    }*/

    class OnAlarm: WakefulBroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1!!.action == "onReveilRing") {
                val testt = Intent(p0, AlarmService::class.java)
                startWakefulService(p0, testt)
            }
        }
    }

    fun getMyActivityNotification(textToSet:String, context: Context): Notification {
        val notificationIntent = Intent(context, PrincipalActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, 0)
        return NotificationCompat.Builder(context, "ChannelIDTest")
                .setContentTitle("JajMeUp prêt")
                .setContentText(textToSet)
                .setSmallIcon(R.drawable.jaccueil)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .build()
    }

    class OnUpdateBetween : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action == "onUpdateTimer") {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                if (sharedPreferences.getString("hours_clock", "-11:-11") != "-11:-11") {
                    Log.d("HELLO", "On change le between time")
                    sharedPreferences.edit().putString("between_time", Alarm.getBetween(sharedPreferences.getString("hours_clock", "-11:-11")!!.toString())).apply()
                    val notifmanager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val mynotif = getMyActivityNotification("Jajmeup à "+ sharedPreferences.getString("hours_clock", "-11:-11").toString() +" dans ("+sharedPreferences.getString("between_time", "-11:-11").toString()+")", context)
                    notifmanager.notify(1, mynotif)
                }
                else {
                    Log.d("HELLO", "Recu update sans reveil "+context.toString())
                    deleteAlarm()
                }
            }
        }
    }
}