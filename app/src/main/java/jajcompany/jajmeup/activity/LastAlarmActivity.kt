package jajcompany.jajmeup.activity

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.last_alarm_layout.*


class LastAlarmActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var urisound: Uri

    companion object IntentOptions{
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, LastAlarmActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.last_alarm_layout)
        textLastAlarm.text = getString(R.string.lastAlarmText)
        stopAlarm.setOnClickListener {
            mediaPlayer.stop()
            finish()
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getString("last_alarm", "defaultalarm") != "defaultalarm") {
            urisound = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("last_alarm", ""))
            mediaPlayer = MediaPlayer.create(this, urisound)
        }
        else {
            mediaPlayer = MediaPlayer.create(this, R.raw.defaultalarm)
        }
        Log.d("HELLO", "Mon reveil "+PreferenceManager.getDefaultSharedPreferences(this).getString("last_alarm", ""))

        mediaPlayer.start()
    }
}