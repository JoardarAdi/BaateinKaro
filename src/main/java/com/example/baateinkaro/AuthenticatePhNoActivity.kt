package com.example.baateinkaro

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class AuthenticatePhNoActivity : AppCompatActivity() {

    private lateinit var phoneNumber: String

    private val verifyTB: TextView by lazy{
        findViewById(R.id.verify_pn_TB)
    }

    private val otpET: TextView by lazy{
        findViewById(R.id.sentcodeEt)
    }
    private val resendBtn: Button by lazy{
        findViewById(R.id.resendBtn)
    }

    private val counterTv: TextView by lazy{
        findViewById(R.id.counterTv)
    }

    private val verificationBtn: Button by lazy{
        findViewById(R.id.verificationBtn)
    }

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mCounter: CountDownTimer



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticate_ph_no)
        initialize()
        startVerify()
    }

    private fun startVerify() {
        startPhoneNumberVerification(phoneNumber)
        startCounter(60000)
        progressDialog = createDialog("Sending verification code", false)
        progressDialog.show()
    }

    private fun startCounter(time: Long) {
        resendBtn.isEnabled = false
        counterTv.isVisible = true
        mCounter = object : CountDownTimer(time, 1000) {
            override fun onFinish() {
                resendBtn.isEnabled = true
                counterTv.isVisible = false
            }

            override fun onTick(timeLeft: Long) {

                counterTv.text = "Seconds Remaining : " + timeLeft / 1000
            }
        }.start()
    }

    private fun initialize() {

        verificationBtn.setOnClickListener {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otpET.text.toString())
            signInWithAuth(credential)
        }
        resendBtn.setOnClickListener {
            resendVerificationCode(phoneNumber, resendToken)
            startCounter(60000)
            progressDialog = createDialog("Sending verification code again", false)
            progressDialog.show()
        }

        try {
            phoneNumber = intent.getStringExtra(PHONE_NUMBER)!!
            verifyTB.text = "Verifying $phoneNumber"

        } catch (e: Exception) {
            Toast.makeText(
                this@AuthenticatePhNoActivity,
                "Number not found! Try Again!!!",
                Toast.LENGTH_SHORT
            ).show()
            onBackPressed()
        }

        auth = Firebase.auth

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                verificationId: String, token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                progressDialog.dismiss()

                storedVerificationId = verificationId
                resendToken = token
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                progressDialog.dismiss()

                val smsCode = credential.smsCode
                otpET.text = smsCode
                Log.i("Verification Completed", "The verification has been completed")
                signInWithAuth(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Invalid Phone Number.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.", Snackbar.LENGTH_SHORT
                    ).show()
                  }
                else {
                    notifyUserAndRetry("Your Phone Number might be wrong or connection error.Retry again!")
                }
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        val options = PhoneAuthOptions.newBuilder(this.auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
    }

    private fun signInWithAuth(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                //First Time Login
                if (task.result?.additionalUserInfo?.isNewUser == true) {
                    showSignUpActivity()
                } else {
                    showHomeActivity()
                }
            } else {

                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }

                notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!")
            }
        }
    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            setCancelable(false)
            create()
            show()
        }
    }

    private fun showLoginActivity() {
        startActivity(
            Intent(
                this,
                PhoneNoActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun showHomeActivity() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    private fun showSignUpActivity() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    private fun resendVerificationCode(
        phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken?
    ) {
        val optionsBuilder =
            PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    override fun onBackPressed() {

    }
}

    //Extensions Functions
    fun Context.createDialog(message: String, isCancelable: Boolean): ProgressDialog {
        return ProgressDialog(this).apply {
            setCancelable(isCancelable)
            setMessage(message)
            setCanceledOnTouchOutside(false)
        }
}