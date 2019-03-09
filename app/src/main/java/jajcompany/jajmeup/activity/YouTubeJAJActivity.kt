package jajcompany.jajmeup.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.WindowManager
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.youtube_layout.*
import android.os.Handler
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener
import jajcompany.jajmeup.utils.YoutubeInformation
import java.util.regex.Pattern

class YouTubeJAJActivity : YouTubeBaseActivity(){

    private var votantString: String = ""
    private lateinit var myYTPlayer: YouTubePlayer

    companion object IntentOptions{
        val API_KEY: String = "AIzaSyBjGxgGofuyFwavGjp4VMlNkfD0_iFcscg"
        var votant:String? = ""
        var lien:String? = ""
        var message:String? = ""
        fun newIntent(context: Context, vot: String?, lie: String?, mess: String?): Intent {
            votant = vot
            lien = YoutubeInformation.getIDFromURL(lie.toString())
            message = mess
            val intent = Intent(context, YouTubeJAJActivity::class.java)
            return intent
        }
    }

    lateinit var youtubePlayerInit: YouTubePlayer.OnInitializedListener

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val handler = Handler()
        val runnableMyYoutubeAlarm = Runnable {
            finish()
            val intentt = YouTubeJAJActivity.newIntent(this, "Ton réveil", sharedPreferences.getString("default_reveil", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"), "Celui qui a voté pour toi n'a pas été assez bon...")
            this.startActivity(intentt)
        }
        val runnableMyLastAlarm = Runnable {
            startLastAlarm()
        }
        setContentView(R.layout.youtube_layout)
        userAlarm.text = getString(R.string.qui_m_a_reveille)+"\n"+votant
        messageAlarm.text = message
        initUI()
        youtubeAlarm.initialize(API_KEY, youtubePlayerInit)
        stopAlarm.setOnClickListener {
            if (votant != "Ton réveil") {
                //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                sharedPreferences.edit().putString("user_wakeup", votant).apply()
                sharedPreferences.edit().putString("message_wakeup", message).apply()
                sharedPreferences.edit().putString("link_wakeup", lien).apply()
                sharedPreferences.edit().putBoolean("on_wakeup", true).apply()
                handler.removeCallbacks(runnableMyYoutubeAlarm)
            }
            else {
                handler.removeCallbacks(runnableMyLastAlarm)
            }
            finish()
        }
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or
        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        var timebeforequit = sharedPreferences.getString("time_before_my_alarm_preference", "3").toInt()
        if (timebeforequit == 75) {
            timebeforequit = 45000
        }
        else {
            timebeforequit *= 60000
        }
        if (votant != "Ton réveil") {
            handler.postDelayed(runnableMyYoutubeAlarm, timebeforequit.toLong())
        }
        else {
            handler.postDelayed(runnableMyLastAlarm, timebeforequit.toLong())
        }
    }

    private fun startLastAlarm() {
        finish()
        val intentt = LastAlarmActivity.newIntent(this)
        this.startActivity(intentt)
    }

    private fun initUI() {
        youtubePlayerInit = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, youtubeAlarm: YouTubePlayer, p2: Boolean) {
                youtubeAlarm.setPlayerStateChangeListener(MyPlayerStateChangeListener())
                youtubeAlarm.loadVideo(lien)
                myYTPlayer = youtubeAlarm
            }

            override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
                startLastAlarm()
                Toast.makeText(applicationContext, "Probleme YouTUBE", Toast.LENGTH_SHORT).show()
            }

            private inner class MyPlayerStateChangeListener : YouTubePlayer.PlayerStateChangeListener {
                internal var playerState = "UNINITIALIZED"

                override fun onLoading() {
                    playerState = "LOADING"
                }

                override fun onLoaded(videoId: String) {
                    playerState = String.format("LOADED %s", videoId)
                }

                override fun onAdStarted() {
                    playerState = "AD_STARTED"
                }

                override fun onVideoStarted() {
                    playerState = "VIDEO_STARTED"
                }

                override fun onVideoEnded() {
                    myYTPlayer.loadVideo(lien)
                    playerState = "VIDEO_ENDED"
                }

                override fun onError(reason: YouTubePlayer.ErrorReason) {
                    playerState = "ERROR ($reason)"
                    startLastAlarm()
                }
            }
        }
    }
}