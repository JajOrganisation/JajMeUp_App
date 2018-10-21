package jajcompany.jajmeup.Models

data class User (val uid: String, val name: String, val reveilDefaultLink: String, val reveilCurrentHour: String, val profilePicture: String?) {
    constructor(): this("", "", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "", null)
}