package com.example.instagramclone.data

data class UserData(
    var userId: String? = null,
    var name: String? = null,
    var userName: String? = null,
    var imageUrl: String? = null,
    var bio: String? = null,
    var following: List<String>? = null,
    var chatId: String ?= null
) {
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "userName" to userName,
        "imageUrl" to imageUrl,
        "bio" to bio,
        "following" to following,
    )
}

data class ChatData(
    val chatId: String ?= null,
    val user1: ChatUser = ChatUser(),
    val user2: ChatUser = ChatUser()
)

data class ChatUser(
    val userId: String ?= null,
    val userName: String ?= null,
    val imageUrl: String ?= null
)

data class Message(
    val sentBy: String ?= "",
    val message: String ?= "",
    val timestamp: String ?= ""
)

data class Status(
    val user: ChatUser = ChatUser(),
    val imageUrl: String ?= null,
    val timestamp: Long? ?= null
)