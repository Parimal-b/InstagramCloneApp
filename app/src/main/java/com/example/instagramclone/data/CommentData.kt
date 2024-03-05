package com.example.instagramclone.data

data class CommentData(
    val commentId: String ?= null,
    val postId: String ?= null,
    val userName: String ?= null,
    val text: String ?= null,
    val timeStamp: Long ?= null
)
