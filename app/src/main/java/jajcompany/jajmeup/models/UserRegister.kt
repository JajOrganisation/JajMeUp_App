package jajcompany.jajmeup.models

data class UserRegister (val uid: String, val name: String, val reveilDefaultLink: String, val reveilCurrentHour: String, val profilePicture: String?, val mynumber:Int = 0, val authorization: Int = 2, val myFriends: List<String> = listOf(), val askingFriends: List<String> = listOf(), val removeFriends: List<String> = listOf()) {
    constructor(): this("", "", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "", null, 0, 2, listOf<String>(), listOf<String>(), listOf<String>())
}