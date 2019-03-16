package jajcompany.jajmeup.models

import java.util.*

data class UserRegister (val uid: String, val name: String, val reveilDefaultLink: String, val reveilCurrentHour: String, val profilePicture: String?, val dateRegistration: Date = Calendar.getInstance().time, val authorization: Int = 2){//, val myFriends: List<String> = listOf(), val askingFriends: List<String> = listOf(), val removeFriends: List<String> = listOf()) {
    constructor(): this("", "", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "", null, Calendar.getInstance().time, 2)//, listOf<String>(), listOf<String>(), listOf<String>())
}