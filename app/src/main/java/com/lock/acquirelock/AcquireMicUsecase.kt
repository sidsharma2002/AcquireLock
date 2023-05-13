package com.lock.acquirelock

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AcquireMicUsecase(
    private val firestore: FirebaseFirestore,
    private val roomData: RoomData
) {

    interface Listener {
        fun acquired(acquiredBy: String?)
        fun released()
        fun error()
    }

    private var snapListener: ListenerRegistration? = null

    fun register(listener: Listener) {
        snapListener = firestore.collection(Constants.COLLC_ROOMS)
            .document(roomData.roomId)
            .addSnapshotListener { value, error ->
                if (error == null) {

                    val acquiredBy: String? = value?.get("lockAcquiredBy") as String?

                    // acquired by me
                    if (acquiredBy?.equals(roomData.userId) == true) {
                        listener.acquired(acquiredBy)
                        isAcquiredByMe = true
                    }

                    // acquired by someone else
                    else if (acquiredBy != null) {
                        isAcquiredByMe = false
                        listener.acquired(acquiredBy)
                    }

                    // released by me
                    else if (acquiredBy?.equals(roomData.userId) == false && isAcquiredByMe) {
                        isAcquiredByMe = false
                        listener.released()
                    }

                    // release by someone else
                    else {
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
            val lockAcquiredBy = snapRef.get("lockAcquiredBy") as String?

            // if lock not acquired by me then return
            if ((!lockAcquiredBy.isNullOrEmpty() && isAcquiredByMe) == false)
                return@runTransaction

            val newLockAcquiredBy = null
            it.update(doc, "lockAcquiredBy", newLockAcquiredBy)
        }.addOnSuccessListener {

        }.addOnCanceledListener {

        }

    }

    fun releaseListeners() {
        snapListener?.remove()
    }

}
