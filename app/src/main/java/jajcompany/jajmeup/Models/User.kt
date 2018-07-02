package jajcompany.jajmeup.Models

data class User (val name: String, val email: String, val profilePicture: String?) {
    constructor(): this("", "", null)
}