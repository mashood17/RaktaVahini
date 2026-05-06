package com.raktavahini.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.raktavahini.app.databinding.ActivityLoginBinding
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // If already logged in, skip login screen
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        binding.btnSendOtp.setOnClickListener { sendOtp() }
        binding.btnVerifyOtp.setOnClickListener { verifyOtp() }
        binding.tvResend.setOnClickListener { resendOtp() }
    }

    private fun sendOtp() {
        val phone = binding.etPhone.text.toString().trim()
        if (phone.length != 10) {
            showSnackbar("Enter a valid 10-digit number")
            return
        }

        val fullPhone = "+91$phone"
        setLoading(true, "Sending OTP...")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(fullPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(verificationCallbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendOtp() {
        val phone = binding.etPhone.text.toString().trim()
        if (phone.length != 10 || resendToken == null) return

        val fullPhone = "+91$phone"
        setLoading(true, "Resending OTP...")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(fullPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(verificationCallbacks)
            .setForceResendingToken(resendToken!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val verificationCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // Auto-verified (same device, instant sign-in)
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            setLoading(false, "")
            showSnackbar("Verification failed: ${e.message}")
        }

        override fun onCodeSent(
            vId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            verificationId = vId
            resendToken = token
            setLoading(false, "")

            // Switch UI to OTP entry
            binding.layoutPhoneEntry.visibility = View.GONE
            binding.layoutOtpEntry.visibility = View.VISIBLE
            showSnackbar("OTP sent to +91${binding.etPhone.text}")
        }
    }

    private fun verifyOtp() {
        val otp = binding.etOtp.text.toString().trim()
        if (otp.length != 6) {
            showSnackbar("Enter the 6-digit OTP")
            return
        }
        if (verificationId.isEmpty()) {
            showSnackbar("Please request OTP first")
            return
        }

        setLoading(true, "Verifying...")
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                setLoading(false, "")
                goToMain()
            }
            .addOnFailureListener { e ->
                setLoading(false, "")
                showSnackbar("Invalid OTP. Try again.")
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean, message: String) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.tvLoadingMessage.text = message
        binding.tvLoadingMessage.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSendOtp.isEnabled = !loading
        binding.btnVerifyOtp.isEnabled = !loading
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}