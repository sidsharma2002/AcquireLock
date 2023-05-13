package com.lock.acquirelock

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

data class CreatedRoomData(
    val creatorUserId: String = "",
    val lockAcquiredBy: String? = null,
    val date: Date? = null
)

@Parcelize
data class RoomData(
    val roomId: String,
    val parentId: String,
    val lockAcquiredBy: String? = null,
    val userId: String
) : Parcelable