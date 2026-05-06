package com.raktavahini.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raktavahini.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If not logged in → go to login
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setupButtons()
        checkAndDisplayUserStatus()

        // Simple logout button (instead of menu)
        binding.btnLogout?.setOnClickListener {
            confirmLogout()
        }
    }

    private fun setupButtons() {
        binding.btnFindBlood.setOnClickListener {
            startActivity(Intent(this, SearchBloodActivity::class.java))
        }

        binding.btnMyProfile.setOnClickListener {
            startActivity(Intent(this, DonorProfileActivity::class.java))
        }

        binding.btnDonationLog.setOnClickListener {
            startActivity(Intent(this, DonationLogActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterDonorActivity::class.java))
        }
    }

    private fun checkAndDisplayUserStatus() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("donors").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // User registered
                    binding.btnMyProfile.visibility = View.VISIBLE
                    binding.btnDonationLog.visibility = View.VISIBLE
                } else {
                    // User not registered yet
                    binding.btnMyProfile.visibility = View.GONE
                    binding.btnDonationLog.visibility = View.GONE
                }
            }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            checkAndDisplayUserStatus()
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                auth.signOut()
                goToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}