package jajcompany.jajmeup.utils

import android.util.Log
import org.json.JSONObject
import java.net.URL
import android.os.StrictMode
import java.util.regex.Pattern

object YoutubeInformation {
    fun getTitleQuietly(youtubeID: String?): String {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            if (youtubeID != null) {
                val embededURL = URL("https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=" +
                        youtubeID + "&format=json"
                ).readText()
                Log.d("HELLO", "JSONRESULT"+embededURL)
                return JSONObject(embededURL).getString("title")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("HELLO", "JSONRESULT ERROR")
            return "ERROR"
        }

        return "ERROR"
    }

    fun getIDFromURL(youtubeURL: String): String {
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(youtubeURL)
        if (matcher.find()) {
            return matcher.group()
        }
        return ""
    }
}