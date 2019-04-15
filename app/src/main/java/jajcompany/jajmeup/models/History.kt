package jajcompany.jajmeup.models

import java.util.*

data class History(var lien: String, var videoname: String, var whowokeup:String, var timeHistory: Date) {
    constructor(): this("", "", "", Date(0))
}