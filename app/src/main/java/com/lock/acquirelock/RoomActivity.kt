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

    private var touchDownTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        roomData = (intent.extras?.getParcelable("room") as RoomData?) ?: return

        btnMic = findViewById(R.id.mic)
        cvMic = findViewById(R.id.cv_mic)
        tvCreatorId = findViewById(R.id.tv_id)

        tvCreatorId.setText(roomData.parentId)

        acquireMicUsecase.register(object : AcquireMicUsecase.Listener {

            override fun acquired(acquiredBy: String?) {
                // TODO vibrate

                if (acquiredBy.equals(roomData.userId)) {
                    cvMic.setCardBackgroundColor(Color.GREEN)
                } else
                    cvMic.setCardBackgroundColor(Color.BLACK)
            }

            override fun released() {
                cvMic.setCardBackgroundColor(resources.getColor(R.color.purple_500))
            }

            override fun error() {

            }

        })

        cvMic.setOnTouchListener { v, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    acquireMicUsecase.tryAcquireMic()

                    touchDownTime = System.currentTimeMillis()
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> {

                    // if (System.currentTimeMillis() - touchDownTime < 1000L) return@setOnTouchListener false // it was a sudden press and release, not a hold.

                    // TODO : release mic if acquired else ignore
                    acquireMicUsecase.releaseAcquiredMic()
                    return@setOnTouchListener true
                }
            }

            return@setOnTouchListener false
        }
    }
}