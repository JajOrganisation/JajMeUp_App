package jajcompany.jajmeup.Models

data class User (val uid: String, val name: String, val reveil: String, val profilePicture: String?) {
    constructor(): this("", "", "", null)
}