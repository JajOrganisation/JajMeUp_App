package jajcompany.jajmeup.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.app.PendingIntent
import android.content.Context
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import jajcompany.jajmeup.R
import jajcompany.jajmeup.activity.PrincipalActivity

class AlarmNotificationService: Service() {

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent!!.getStringExtra("heureReveil")
        val inputbetween = intent!!.getStringExtra("heureBetween")

        startForeground(1, getMyActivityNotification("Jajmeup à $input"))
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun getMyActivityNotification(textToSet: String): Notification{
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

    fun updateNotification(textToSet: String) {
        val mynotif = getMyActivityNotification(textToSet)
        val notifmanager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifmanager.notify(1, mynotif)
    }
}