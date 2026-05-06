package com.raktavahini.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.raktavahini.app.databinding.ActivityDonorProfileBinding
import java.util.*
import java.text.SimpleDateFormat

class DonorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonorProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var profileListener: ListenerRegistration? = null
    private var currentDonor: Donor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Donor Profile"

        val uid = auth.currentUser?.uid

        if (uid == null) {
            binding.tvNotRegistered.visibility = View.VISIBLE
          //  binding.cardProfile.visibility = View.GONE
            binding.btnLogDonation.visibility = View.GONE
            binding.btnViewLog.visibility = View.GONE
            return
        }

        attachProfileListener(uid)

        binding.btnLogDonation.setOnClickListener {

            if (!NetworkUtils.isOnline(this)) {

                Snackbar.make(
                    binding.root,
                    "⚠️ No internet connection",
                    Snackbar.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            logDonationToday(uid)
        }
        binding.btnViewLog.setOnClickListener {
            startActivity(Intent(this, DonationLogActivity::class.java))
        }
    }

    private fun attachProfileListener(uid: String) {
        profileListener = db.collection("donors").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null) return@addSnapshotListener

                if (!doc.exists()) {
                    // Not yet registered — prompt them
                    binding.tvNotRegistered.visibility = View.VISIBLE
                    binding.tvNotRegistered.text = "You haven't registered as a donor yet.\nGo back and tap 'Register as Donor'."
                //    binding.cardProfile.visibility = View.GONE
                    binding.btnLogDonation.visibility = View.GONE
                    binding.btnViewLog.visibility = View.GONE
                    return@addSnapshotListener
                }

                binding.tvNotRegistered.visibility = View.GONE
               // binding.cardProfile.visibility = View.VISIBLE
                binding.btnLogDonation.visibility = View.VISIBLE
                binding.btnViewLog.visibility = View.VISIBLE

                val donor = Donor(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    bloodGroup = doc.getString("bloodGroup") ?: "",
                    city = doc.getString("city") ?: "",
                    phone = doc.getString("phone") ?: "",
                    lastDonationDate = doc.getLong("lastDonationDate") ?: 0L,
                    isAvailable = doc.getBoolean("isAvailable") ?: true
                )
                currentDonor = donor

                binding.tvProfileName.text = donor.name
                binding.tvProfileBloodGroup.text = donor.bloodGroup
                binding.tvProfileCity.text = "📍 ${donor.city}"

                // Update toggle without triggering listener
                binding.switchAvailability.setOnCheckedChangeListener(null)
                binding.switchAvailability.isChecked = donor.isAvailable
                reAttachToggleListener(uid)

                if (donor.isEligible()) {
                    binding.tvEligibilityStatus.text = "✅ You are eligible to donate!"
                    binding.tvEligibilityStatus.setTextColor(getColor(R.color.eligible_green))
                } else {
                    val days = donor.daysUntilEligible()
                    binding.tvEligibilityStatus.text = "⏳ Eligible again in $days days"
                    binding.tvEligibilityStatus.setTextColor(getColor(R.color.medium_grey))
                }
            }
    }

    private fun reAttachToggleListener(uid: String) {
        binding.switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            db.collection("donors").document(uid)
                .update("isAvailable", isChecked)
                .addOnSuccessListener {
                    val msg = if (isChecked) "✅ You are now visible to searchers"
                    else "⏸ Hidden from search results"
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun logDonationToday(uid: String) {
        val todayMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(todayMillis))

        db.collection("donors").document(uid)
            .update(
                "lastDonationDate", todayMillis,
                "donationHistory", FieldValue.arrayUnion(todayFormatted)
            )
            .addOnSuccessListener {
                Snackbar.make(binding.root, "🩸 Thank you for saving a life!", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.eligible_green))
                    .setTextColor(getColor(R.color.white))
                    .show()
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Failed to log donation. Check connection.", Snackbar.LENGTH_SHORT).show()
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
        profileListener?.remove()
    }
}