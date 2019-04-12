package jajcompany.jajmeup.utils

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.WakefulBroadcastReceiver
import android.util.Log
import jajcompany.jajmeup.R
import jajcompany.jajmeup.activity.LoadingAlarm
import jajcompany.jajmeup.activity.PrincipalActivity
import java.util.*


class AlarmService : IntentService("AlarmService") {

    override fun onHandleIntent(intent: Intent?) {
       /* val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PrincipalActivity.applicationContext())
        val mynotif = getMyActivityNotification("Ton réveil sonne bientôt ! ("+intent!!.getStringExtra("heureReveil")+")")
        val notifmanager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifmanager.notify(1, mynotif)
        Log.d("HELLO", "Belle milli "+Alarm.getBetweenMinute(intent.getStringExtra("heureReveil")))
        val getMilli = Alarm.getBetweenMinute(intent.getStringExtra("heureReveil"))
        if (getMilli > 0) {
            var count = 0
            while(count < getMilli){
                Thread.sleep(1000)
                count += 1000
                Log.d("HELLO", "On tourne dans l alarmservice")
            }
        }
        Log.d("HELLO", "Belle Heure "+intent.getStringExtra("heureReveil"))*/
        //if (sharedPreferences.getString("hours_clock", "-11:-11")!!.toString() != "-11:-11") {
        val i = Intent(this, LoadingAlarm::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        /*}
        else {
           WakefulBroadcastReceiver.completeWakefulIntent(intent)
        }*/
    }
    fun getMyActivityNotification(textToSet: String): Notification {
        val notificationIntent = Intent(this, PrincipalActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)
        return NotificationCompat.Builder(this, "ChannelIDTest")
                .setContentTitle("JajMeUp prêt")
                .setContentText(textToSet)
                .setSmallIcon(R.drawable.jaccueil)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .build()
    }
}