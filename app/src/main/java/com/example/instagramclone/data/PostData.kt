package com.example.instagramclone.data

import android.os.Parcel
import android.os.Parcelable

data class PostData(
    val postId: String ?= null,
    val userId: String ?= null,
    val userName: String ?= null,
    val userImage: String ?= null,
    val postImage: String ?= null,
    val postDescription: String ?= null,
    val time:Long ?= null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(userImage)
        parcel.writeString(postImage)
        parcel.writeString(postDescription)
        parcel.writeValue(time)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostData> {
        override fun createFromParcel(parcel: Parcel): PostData {
            return PostData(parcel)
        }

        override fun newArray(size: Int): Array<PostData?> {
            return arrayOfNulls(size)
        }
    }
}
