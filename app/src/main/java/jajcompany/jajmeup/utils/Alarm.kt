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
        intentAlarmPrincipale.putExtra("heureReveil", String.format("%02d",hours)+":"+String.format("%02d",minutes))
        intentAlarmPrincipale.action = "onReveilRing"
        val pendingAlarmPrincipal = PendingIntent.getBroadcast(contextApp, 101, intentAlarmPrincipale, PendingIntent.FLAG_UPDATE_CURRENT)
        val timeZone = TimeZone.getDefault()
        val cal: Calendar = Calendar.getInstance(timeZone)
        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        /*var mintmp = Calendar.getInstance(timeZone).get(Calendar.MINUTE)+12
        var hourtmp =  Calendar.getInstance(timeZone).get(Calendar.HOUR_OF_DAY)
        if (mintmp >= 60) {
            mintmp -= 60
            hourtmp += 1
            if (hourtmp >= 24)
                hourtmp -= 24
        }
        Log.d("HELLO", "Hours $hours CurrentHours $hourtmp Minutes $minutes MinutesCurrent+10 $mintmp")
        if ((hours == hourtmp) && (minutes <= mintmp))
            cal.set(Calendar.MINUTE, minutes)
        else {
            Log.d("HELLO", "On enleve 10")
            if ( (minutes-11) < 0)
            {
                if ((hours-1)<0)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                else
                    cal.set(Calendar.HOUR_OF_DAY, hours-1)
                cal.set(Calendar.MINUTE, 59-(11-minutes))
            }
            else
                cal.set(Calendar.MINUTE, minutes - 11)
        }*/
        cal.set(Calendar.SECOND, 0)
        Log.d("HELLO", "HEURE "+cal.timeInMillis)
        var time = cal.timeInMillis - cal.timeInMillis % 60000

        if (System.currentTimeMillis() > time) {
            time += 1000 * 60 * 60 * 24
        }
        Log.d("HELLO", "TIME "+time.toString())
        alarmManagerPrincipal = contextApp.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val ac = AlarmManager.AlarmClockInfo(time, pendingAlarmPrincipal)
        alarmManagerPrincipal.setAlarmClock(ac, pendingAlarmPrincipal)
        //alarmManagerPrincipal.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingAlarmPrincipal)
        switchAlarm = switchA
        setNotif(hours, minutes)
        val intentAlarmBetween = Intent(contextApp, OnUpdateBetween::class.java)
        intentAlarmBetween.action = "onUpdateTimer"
        intentAlarmBetween.putExtra("heureReveil", String.format("%02d",hours)+":"+String.format("%02d",minutes))
        val pendingAlarmBetween = PendingIntent.getBroadcast(contextApp, 102, intentAlarmBetween, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManagerBetween = contextApp.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManagerBetween.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), 1000*60, pendingAlarmBetween)
        FireStore.updateCurrentUser(reveilCurrent =  "up")
    }

    fun setNotif(hours: Int, minutes: Int) {
        val contextApp = PrincipalActivity.applicationContext()
        val hoursString = String.format("%02d", hours)
        val minutesString = String.format("%02d", minutes)
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
            val pendingIntent = PendingIntent.getBroadcast(contextApp, 101, intentPrincipal, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManagerPrincipal = contextApp.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManagerPrincipal.cancel(pendingIntent)
            pendingIntent.cancel()
            val serviceAlarm = Intent(contextApp, AlarmService::class.java)
            contextApp.stopService(serviceAlarm)
            val intentBetween = Intent(contextApp, OnUpdateBetween::class.java)
            intentBetween.action = "onUpdateTimer"
            val pendingIntentBetween = PendingIntent.getBroadcast(contextApp, 102, intentBetween, PendingIntent.FLAG_UPDATE_CURRENT)
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
        val timeZone = TimeZone.getDefault()
        val calStart: Calendar = Calendar.getInstance(timeZone)
        calStart.set(Calendar.HOUR_OF_DAY, alarmTotalDepart.split(':')[0].toInt())
        calStart.set(Calendar.MINUTE, alarmTotalDepart.split(':')[1].toInt())
        calStart.set(Calendar.SECOND, 0)
        val calCurrentTime = Calendar.getInstance(timeZone)

        var milliStart = calStart.timeInMillis
        val milliCurrent = calCurrentTime.timeInMillis

        if (milliStart < milliCurrent)
            milliStart += 1000 * 60 * 60 * 24

        Log.d("HELLO", "Start "+milliStart.toString()+" Current "+milliCurrent.toString())

        val diff = milliStart - milliCurrent

        Log.d("HELLO", "Diff "+diff.toString()+" "+((diff / (1000*60*60))%24).toString()+":"+((diff / (1000*60))%60).toString())

        return String.format("%02d", (diff / (1000*60*60))%24)+":"+ String.format("%02d", ((diff / (1000*60))%60))
    }

    fun getBetweenMinute(alarmTotalDepart: String): Long {
        val timeZone = TimeZone.getDefault()
        val calStart: Calendar = Calendar.getInstance(timeZone)
        calStart.set(Calendar.HOUR_OF_DAY, alarmTotalDepart.split(':')[0].toInt())
        calStart.set(Calendar.MINUTE, alarmTotalDepart.split(':')[1].toInt())
        calStart.set(Calendar.SECOND, 0)
        val calCurrentTime = Calendar.getInstance(timeZone)

        val milliStart = calStart.timeInMillis
        val milliCurrent = calCurrentTime.timeInMillis

        if (milliStart <= milliCurrent)
            return 0

        Log.d("HELLO", "Start "+milliStart.toString()+" Current "+milliCurrent.toString())

        val diff = milliStart - milliCurrent

        Log.d("HELLO", "DIFF MINUTE "+(((diff / (1000*60)))*60*1000))

        return (((diff / (1000*60)))*60*1000)
    }

    class OnAlarm: WakefulBroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == "onReveilRing") {
                Log.d("HELLO", "RECU")
                val intentWake = Intent(context, AlarmService::class.java)
                intentWake.putExtra("heureReveil", intent.getStringExtra("heureReveil"))
                startWakefulService(context, intentWake)
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
                    var mynotif = getMyActivityNotification("Jajmeup à "+ intent.getStringExtra("heureReveil"), context)
                    if (getBetweenMinute(intent.getStringExtra("heureReveil")) <= 600000)
                        mynotif = getMyActivityNotification("Ton réveil sonne bientôt oui ! ("+intent.getStringExtra("heureReveil")+")", context)
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