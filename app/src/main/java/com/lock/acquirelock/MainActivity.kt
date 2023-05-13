package com.lock.acquirelock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var btnCreate: Button
    private lateinit var btnJoin: Button
    private lateinit var etId: EditText

    private val userId: String = (0..1000000).random().toString()

    private val createRoomUseCase by lazy {
        CreateRoomUseCase(Firebase.firestore, userId)
    }

    private val getRoomDataFromIdUsecase by lazy {
        GetRoomDataFromIdUsecase(Firebase.firestore, userId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCreate = findViewById(R.id.btn_create)
        btnJoin = findViewById(R.id.btn_join)
        etId = findViewById(R.id.et_room)

        setClickListeners()
    }

    private fun setClickListeners() {
        btnCreate.setOnClickListener {
            Thread {
                val roomData = createRoomUseCase.createSync()

                Intent(this, RoomActivity::class.java).apply {
                    this.putExtra("room", roomData)
                    startActivity(this)
                }
            }.start()

        }

        btnJoin.setOnClickListener {
            Thread {
                val roomData = getRoomDataFromIdUsecase.getSync(etId.text.toString()) ?: return@Thread

                Intent(this, RoomActivity::class.java).apply {
                    this.putExtra("room", roomData)
                    startActivity(this)
                }
            }.start()
        }
    }
}