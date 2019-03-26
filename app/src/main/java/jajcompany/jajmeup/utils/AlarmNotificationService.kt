package jajcompany.jajmeup.utils

import android.app.Service
import android.content.Intent
import android.app.PendingIntent
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

        val notificationIntent = Intent(this, PrincipalActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, "ChannelIDTest")
                .setContentTitle("Jajmeup à $input")
                .setContentText(input)
                .setSmallIcon(R.drawable.jaccueil)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(1, notification)

        //do heavy work on a background thread
        //stopSelf();

        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}