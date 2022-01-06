package com.example.baateinkaro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.hbb20.CountryCodePicker

const val PHONE_NUMBER = "PHONE NUMBER"

class PhoneNoActivity : AppCompatActivity() {

    private lateinit var countryCode: String
    private lateinit var phoneNo: String

    private val phoneNoEditText: EditText by lazy{
        findViewById(R.id.phoneNoET)
    }

    private  val continueButton: Button by lazy{
        findViewById(R.id.contBtn)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_no)

        phoneNoEditText.addTextChangedListener {
            if (it != null) {
                continueButton.isEnabled = !((it.isNullOrEmpty()) || (it.length < 10))
            }
        }

        continueButton.setOnClickListener {
                validateNumber()
        }
    }

    private fun validateNumber() {

        countryCode = findViewById<CountryCodePicker>(R.id.ccp).selectedCountryCodeWithPlus.trim()
        phoneNo = phoneNoEditText.text.toString().trim()

        //val fullPhNo = "$countryCode $phoneNo"
        //val phNo  = countryCode + (phoneNoEditText.text.toString())
        val phNo = countryCode + phoneNo


        if(phoneNo.length > 10){
            Toast.makeText(this@PhoneNoActivity, "PLEASE ENTER A VALID NUMBER", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this@PhoneNoActivity, "Verifying $phNo", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AuthenticatePhNoActivity::class.java).putExtra(PHONE_NUMBER, phNo))
        }

    }
}