package jajcompany.jajmeup.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.clock_layout.*
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
        userAlarm.setText(votant)
        messageAlarm.setText(message)
        initUI()
        youtubeAlarm.initialize(API_KEY, youtubePlayerInit)
        stopAlarm.setOnClickListener {
            finish()
        }
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