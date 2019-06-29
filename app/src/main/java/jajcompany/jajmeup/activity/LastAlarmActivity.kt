package jajcompany.jajmeup.activity

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.youtube.player.YouTubeBaseActivity
import jajcompany.jajmeup.R
import jajcompany.jajmeup.utils.Alarm
import kotlinx.android.synthetic.main.last_alarm_layout.*
import java.io.File


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
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            sharedPreferences.edit().putBoolean("on_wakeup_my_alarm_clock", true).apply()
            finish()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            this.setShowWhenLocked(true)
            this.setTurnScreenOn(true)
        } else {
            val windoWw = window
            windoWw.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            windoWw.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        val audioManager: AudioManager = getSystemService(YouTubeBaseActivity.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val prefVolume = PreferenceManager.getDefaultSharedPreferences(this).getInt("volume_reveil", 6)
        val finalVolume = ((prefVolume*10)*maxVolume)/100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, finalVolume, 0)
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("last_alarm", "defaultalarm") != "defaultalarm") {
            urisound = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("last_alarm", ""))
            if (File(urisound.path).exists()){
                mediaPlayer = MediaPlayer.create(this, urisound)
            }
            else {
                mediaPlayer = MediaPlayer.create(this, R.raw.defaultalarm)
            }
        }
        else {
            mediaPlayer = MediaPlayer.create(this, R.raw.defaultalarm)
        }
        Log.d("HELLO", "Mon reveil "+PreferenceManager.getDefaultSharedPreferences(this).getString("last_alarm", ""))
        Alarm.deleteAlarm()
        Alarm.unsetNotif()
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }
}