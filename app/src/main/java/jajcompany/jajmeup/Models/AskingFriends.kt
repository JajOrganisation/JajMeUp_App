package jajcompany.jajmeup.Models

data class AskingFriends (val uid: String, val name: String, val profilePicture: String?) {
    constructor(): this("", "", "")
}