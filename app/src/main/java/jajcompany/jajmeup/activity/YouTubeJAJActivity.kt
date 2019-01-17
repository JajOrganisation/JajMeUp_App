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

class YouTubeJAJActivity : YouTubeBaseActivity(){

    companion object IntentOptions{
        val API_KEY: String = "AIzaSyBjGxgGofuyFwavGjp4VMlNkfD0_iFcscg"
        var votant:String? = ""
        var lien:String? = ""
        var message:String? = ""
        fun newIntent(context: Context, vot: String?, lie: String?, mess: String?): Intent {
            votant = vot
            lien = lie
            message = mess
            val intent = Intent(context, YouTubeJAJActivity::class.java)
            return intent
        }

        fun Intent.setVotant(votantset: String){
            votant = votantset
        }
        fun Intent.setLien(lienset: String){
            lien = lienset
        }
        fun Intent.setMessage(messageset: String){
            message = messageset
        }
    }

    lateinit var youtubePlayerInit: YouTubePlayer.OnInitializedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.youtube_layout)
        userAlarm.text = "Tu as été réveillé par "+votant
        messageAlarm.text = message
        initUI()
        youtubeAlarm.initialize(API_KEY, youtubePlayerInit)
        stopAlarm.setOnClickListener {
            if (votant != "Ton réveil") {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                sharedPreferences.edit().putString("user_wakeup", votant).apply()
                sharedPreferences.edit().putString("message_wakeup", message).apply()
                sharedPreferences.edit().putString("link_wakeup", lien).apply()
                sharedPreferences.edit().putBoolean("on_wakeup", true).apply()
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
    }

    private fun initUI() {
        youtubePlayerInit = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, youtubeAlarm: YouTubePlayer?, p2: Boolean) {
                youtubeAlarm?.loadVideo(lien)

            }

            override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
                Toast.makeText(applicationContext, "Probleme YouTUBE", Toast.LENGTH_SHORT).show()
            }

        }
    }
}