package jajcompany.jajmeup.utils

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import jajcompany.jajmeup.R
import jajcompany.jajmeup.activity.PrincipalActivity

class AlarmPrevious : IntentService("AlarmPreviousService") {
    override fun onHandleIntent(intent: Intent?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val hour = sharedPreferences.getString("hours_clock", "-11:-11")!!.toString()
        val mynotif = getMyActivityNotification("Ton réveil sonne bientôt ! ($hour)")
        val notifmanager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifmanager.notify(1, mynotif)
        Thread.sleep(1000)
        val mynotiff = getMyActivityNotification("Ton réveil sonne bientôt hehe! ($hour)")
        notifmanager.notify(1, mynotiff)

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