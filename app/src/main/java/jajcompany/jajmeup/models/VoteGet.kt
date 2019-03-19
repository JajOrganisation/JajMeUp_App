package jajcompany.jajmeup.models

data class VoteGet(val lien: String, val videoname: String, val votant:String, val votantuid:String, val message: String) {
    constructor(): this("", "", "", "", "")
}