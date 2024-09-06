package uk.ac.aber.dcs.chm9360.travelbuddy.model

data class FriendRequest(
    val senderId: String = "",
    val receiverId: String = "",
    val senderEmail: String = "",
    val senderUsername: String = ""
)