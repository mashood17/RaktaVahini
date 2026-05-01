package com.raktavahini.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.raktavahini.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate to Search screen
        binding.btnFindBlood.setOnClickListener {
            startActivity(Intent(this, SearchBloodActivity::class.java))
        }

        // Navigate to Registration screen
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterDonorActivity::class.java))
        }

        // Navigate to My Profile screen
        binding.btnMyProfile.setOnClickListener {
            startActivity(Intent(this, DonorProfileActivity::class.java))
        }

        // Navigate to Donation Log screen
        binding.btnDonationLog.setOnClickListener {
            startActivity(Intent(this, DonationLogActivity::class.java))
        }
    }
}