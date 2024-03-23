package com.example.instagramclone.data

data class UserData(
    var userId: String? = null,
    var name: String? = null,
    var userName: String? = null,
    var imageUrl: String? = null,
    var bio: String? = null,
    var following: List<String>? = null,
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