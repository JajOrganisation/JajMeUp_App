package jajcompany.jajmeup.models

data class UserRegister (val uid: String, val name: String, val reveilDefaultLink: String, val reveilCurrentHour: String, val profilePicture: String?, val mynumber:Int = 0, val authorization: Int = 2) {
    constructor(): this("", "", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "", null, 0, 2)
}