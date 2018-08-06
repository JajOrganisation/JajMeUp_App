package jajcompany.jajmeup.Models

data class Vote(val lien: String, val votant:String) {
    constructor(): this("", "")
}