package jajcompany.jajmeup.Models

data class User (val name: String, val reveil: String, val profilePicture: String?) {
    constructor(): this("", "", null)
}