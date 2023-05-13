package com.lock.acquirelock

import androidx.annotation.WorkerThread
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CreateRoomUseCase(
    private val firestore: FirebaseFirestore,
    private val userId: String
) {

    @WorkerThread
    fun createSync(): RoomData? {

        val createdRoomData = CreatedRoomData(userId, null, Date())

        val latch = CountDownLatch(1)

        var roomData: RoomData? = null

        return try {
            val result = firestore.collection(Constants.COLLC_ROOMS)
                .add(createdRoomData).addOnSuccessListener {
                    roomData = RoomData(
                        it.id,
                        createdRoomData.creatorUserId,
                        "",
                        userId
                    )
                    latch.countDown()
                }

            latch.await(10, TimeUnit.SECONDS)
            roomData
        } catch (e: Exception) {
            return roomData
        }
    }

}