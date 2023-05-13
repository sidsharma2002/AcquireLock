package com.lock.acquirelock

import androidx.annotation.WorkerThread
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class GetRoomDataFromIdUsecase(
    private val firestore: FirebaseFirestore,
    private val userId: String
) {

    @WorkerThread
    fun getSync(creatorUserId: String): RoomData? {

        var roomData: RoomData? = null
        val latch = CountDownLatch(1)

        firestore.collection(Constants.COLLC_ROOMS).whereEqualTo("creatorUserId", creatorUserId)
            .get()
            .addOnSuccessListener {

                if (it.documents.size == 0) return@addOnSuccessListener

                val createdRoomData = it.documents[0].toObject(CreatedRoomData::class.java)

                if (createdRoomData == null) {
                    latch.countDown()
                    return@addOnSuccessListener
                }

                roomData = RoomData(
                    it.documents[0].id,
                    createdRoomData.creatorUserId,
                    createdRoomData.lockAcquiredBy,
                    userId
                )
                latch.countDown()
            }

        latch.await(10, TimeUnit.SECONDS)
        return roomData
    }

}
