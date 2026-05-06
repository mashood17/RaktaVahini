package com.raktavahini.app

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.raktavahini.app.databinding.ActivityDonationLogBinding

class DonationLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonationLogBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var logListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Donation History"

       // binding.recyclerLog.layoutManager = LinearLayoutManager(this)

        val uid = auth.currentUser?.uid
        if (uid == null) {
            binding.tvLogEmpty.text = "Please log in to see your donation history."
            //binding.tvLogEmpty.visibility = View.VISIBLE
            return
        }

        listenToLog(uid)
    }

    private fun listenToLog(uid: String) {
        logListener = db.collection("donors").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) return@addSnapshotListener

                @Suppress("UNCHECKED_CAST")
                val history = (doc.get("donationHistory") as? List<String>)
                    ?.sortedDescending() ?: emptyList()
                binding.tvTotalDonations.text = history.size.toString()

                val lastDate = doc.getLong("lastDonationDate") ?: 0L

                if (history.isEmpty()) {
                    //binding.tvLogEmpty.visibility = View.VISIBLE
                    binding.tvLogEmpty.text = "No donations logged yet.\nTap 'Log a Donation Today' from your profile."
                   // binding.recyclerLog.visibility = View.GONE
                    binding.tvNextEligible.visibility = View.GONE
                } else {
                    binding.tvLogEmpty.visibility = View.GONE
                   // binding.recyclerLog.visibility = View.VISIBLE

                    // Show next eligibility date
                    val donor = Donor(lastDonationDate = lastDate)
                    if (donor.isEligible()) {
                        binding.tvNextEligible.text = "✅ You are currently eligible to donate"
                        binding.tvNextEligible.setTextColor(getColor(R.color.eligible_green))
                    } else {
                        binding.tvNextEligible.text = "⏳ Eligible again in ${donor.daysUntilEligible()} days"
                        binding.tvNextEligible.setTextColor(getColor(R.color.medium_grey))
                    }
                    binding.tvNextEligible.visibility = View.VISIBLE

                    //binding.recyclerLog.adapter = DonationLogAdapter(history)
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        logListener?.remove()
    }
}