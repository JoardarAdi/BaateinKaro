package com.example.baateinkaro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class IntroActivity : AppCompatActivity() {

    private val contBtn: Button by lazy{
        findViewById(R.id.continueBtn)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        contBtn.setOnClickListener {
            startActivity(Intent(this, PhoneNoActivity::class.java))
        }

    }
}