package jajcompany.jajmeup.models

import com.google.firebase.firestore.DocumentReference
import java.lang.ref.Reference
import java.util.*

data class Vote(val lien: String, val videoname: String, val votant:String, val message: String, val timeVote:Date) {
    constructor(): this("", "", "", "", Date(0))
}