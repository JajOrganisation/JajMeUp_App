package jajcompany.jajmeup.Utils

import android.util.Log
import org.json.JSONObject
import java.net.URL
import android.os.StrictMode

object YoutubeInformation {
    fun getTitleQuietly(youtubeID: String?): String {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            if (youtubeID != null) {
                Log.d("HELLO", "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=" +
                        youtubeID + "&format=json")
                val embededURL = URL("https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=" +
                        youtubeID + "&format=json"
                ).readText()

                return JSONObject(embededURL).getString("title")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }
}