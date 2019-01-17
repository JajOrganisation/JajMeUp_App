package jajcompany.jajmeup.models

data class AskingFriends (val uid: String, val name: String, val profilePicture: String?) {
    constructor(): this("", "", "")
}