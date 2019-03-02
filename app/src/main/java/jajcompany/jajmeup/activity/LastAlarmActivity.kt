package jajcompany.jajmeup.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.last_alarm_layout.*



class LastAlarmActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer

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
        val urisound = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("last_alarm", ""))
        Log.d("HELLO", "Mon reveil "+PreferenceManager.getDefaultSharedPreferences(this).getString("last_alarm", ""))
        val data = Uri.parse (Environment.getExternalStorageDirectory ().path.toString() + "/nevergonna.mp3")
        mediaPlayer = MediaPlayer.create(this, urisound)
        //mediaPlayer.setOnPreparedListener( { mp ->  mp.start() })
        //mediaPlayer.setOnPreparedListener(MediaPlayer.OnPreparedListener { mp -> mp.start() })
        mediaPlayer.start()
    }
}