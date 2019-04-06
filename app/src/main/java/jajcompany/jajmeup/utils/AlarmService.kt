package jajcompany.jajmeup.utils

import android.app.IntentService
import android.content.Intent
import jajcompany.jajmeup.activity.LoadingAlarm


class AlarmService : IntentService("AlarmService") {

    override fun onHandleIntent(intent: Intent?) {
        val i = Intent(this, LoadingAlarm::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }
}