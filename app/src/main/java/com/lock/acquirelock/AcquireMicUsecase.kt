package com.lock.acquirelock

import com.google.firebase.firestore.FirebaseFirestore

class AcquireMicUsecase(
    private val firestore: FirebaseFirestore,
    private val roomData: RoomData
) {

    interface Listener {
        fun acquired(acquiredBy: String?)
        fun released()
        fun error()
    }

    fun register(listener: Listener) {
        firestore.collection(Constants.COLLC_ROOMS)
            .document(roomData.roomId)
            .addSnapshotListener { value, error ->
                if (error == null) {

                    val acquiredBy: String? = value?.get("lockAcquiredBy") as String?

                    if (acquiredBy?.equals(roomData.userId) == true) {
                        listener.acquired(acquiredBy)
                        isAcquiredByMe = true
                    } else if (acquiredBy != null) {
                        // acquired by someone else
                        isAcquiredByMe = false
                        listener.acquired(acquiredBy)
                    } else if (acquiredBy?.equals(roomData.userId) == false && isAcquiredByMe) { // released by me
                        isAcquiredByMe = false
                        listener.released()
                    } else {
                        // release by someone else
                        listener.released()
                        isAcquiredByMe = false
                    }

                } else {
                    listener.error()
                    isAcquiredByMe = false
                }
            }
    }

    private var isAcquiredByMe: Boolean = false

    fun tryAcquireMic() {
        val doc = firestore.collection(Constants.COLLC_ROOMS).document(roomData.roomId)

        firestore.runTransaction {
            val snapRef = it.get(doc)

            // return if already acquired
            val lockAcquiredBy = snapRef.get("lockAcquiredBy") as String?
            if (!lockAcquiredBy.isNullOrEmpty()) return@runTransaction

            val newLockAcquiredBy = roomData.userId
            it.update(doc, "lockAcquiredBy", newLockAcquiredBy)
        }.addOnSuccessListener {

        }.addOnCanceledListener {

        }
    }

    fun releaseAcquiredMic() {

        val doc = firestore.collection(Constants.COLLC_ROOMS).document(roomData.roomId)

        firestore.runTransaction {
            val snapRef = it.get(doc)

            // if lock not acquired by me then return
            val lockAcquiredBy = snapRef.get("lockAcquiredBy") as String?
            if ((!lockAcquiredBy.isNullOrEmpty() && isAcquiredByMe) == false)
                return@runTransaction

            val newLockAcquiredBy = null
            it.update(doc, "lockAcquiredBy", newLockAcquiredBy)
        }.addOnSuccessListener {

        }.addOnCanceledListener {

        }

    }

}
