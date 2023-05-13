package com.lock.acquirelock

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet.Motion
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RoomActivity : AppCompatActivity() {

    private lateinit var roomData: RoomData
    private lateinit var btnMic: TextView
    private lateinit var cvMic: CardView
    private lateinit var tvCreatorId: TextView

    private val acquireMicUsecase by lazy {
        AcquireMicUsecase(Firebase.firestore, roomData)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        roomData = (intent.extras?.getParcelable("room") as RoomData?) ?: return

        btnMic = findViewById(R.id.mic)
        cvMic = findViewById(R.id.cv_mic)
        tvCreatorId = findViewById(R.id.tv_id)

        tvCreatorId.setText(roomData.parentId)

        cvMic.setOnTouchListener { v, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    acquireMicUsecase.tryAcquireMic()
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> {
                    acquireMicUsecase.releaseAcquiredMic()
                    return@setOnTouchListener true
                }
            }

            return@setOnTouchListener false
        }
    }

    override fun onResume() {
        super.onResume()

        acquireMicUsecase.register(object : AcquireMicUsecase.Listener {

            override fun acquired(acquiredBy: String?) {
                if (acquiredBy.equals(roomData.userId)) {
                    cvMic.setCardBackgroundColor(Color.GREEN)
                } else
                    cvMic.setCardBackgroundColor(Color.BLACK)
            }

            override fun released() {
                cvMic.setCardBackgroundColor(resources.getColor(R.color.purple_500))
            }

            override fun error() {
                // TODO
            }

        })
    }

    override fun onPause() {
        // If user exits app while the mic is acquired by him, then the system will become in a buggy state.
        // To avoid this, release in onPause itself.
        acquireMicUsecase.releaseAcquiredMic()
        acquireMicUsecase.releaseListeners()
        super.onPause()
    }
}