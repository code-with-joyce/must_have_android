package com.example.activityandfragment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingButton() // ❹
    }

    fun settingButton(){
        val button = findViewById<Button>(R.id.button) // ❶
        button.setOnClickListener {
            val intent = Intent(this,SubActivity::class.java) // ❷
            startActivity(intent) // ❸
        }
    }
}
