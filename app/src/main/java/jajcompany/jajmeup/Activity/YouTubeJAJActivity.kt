package jajcompany.jajmeup.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.youtube_layout.*

class YouTubeJAJActivity : YouTubeBaseActivity(){

    companion object {
        val API_KEY: String = "AIzaSyBjGxgGofuyFwavGjp4VMlNkfD0_iFcscg"
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, YouTubeJAJActivity::class.java)
            return intent
        }
    }

    lateinit var youtubePlayerInit: YouTubePlayer.OnInitializedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.youtube_layout)
        initUI()
        youtubeAlarm.initialize(API_KEY, youtubePlayerInit)
    }

    private fun initUI() {
        youtubePlayerInit = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, youtubeAlarm: YouTubePlayer?, p2: Boolean) {
                youtubeAlarm?.loadVideo("dQw4w9WgXcQ")
            }

            override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
                Toast.makeText(applicationContext, "Probleme YouTUBE", Toast.LENGTH_SHORT).show()
            }

        }
    }
}