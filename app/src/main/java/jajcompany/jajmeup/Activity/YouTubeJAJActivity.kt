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
        val API_KEY: String = "A_REMPLACER"
        var votant:String? = ""
        var lien:String? = ""
        fun newIntent(context: Context, vot: String?, lie: String?): Intent {
            Log.d("HELLO YT ", vot)
            votant = vot
            lien = lie
            val intent = Intent(context, YouTubeJAJActivity::class.java)
            return intent
        }

        fun Intent.setVotant(votantset: String){
            votant = votantset
        }
        fun Intent.setLien(lienset: String){
            lien = lienset
        }
    }

    lateinit var youtubePlayerInit: YouTubePlayer.OnInitializedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("HELLO YT 2", votant)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.youtube_layout)
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