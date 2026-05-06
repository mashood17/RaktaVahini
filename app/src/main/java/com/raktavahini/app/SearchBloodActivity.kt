package com.raktavahini.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.raktavahini.app.databinding.ActivitySearchBloodBinding
import android.view.View

class SearchBloodActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBloodBinding
    private lateinit var db: FirebaseFirestore

    private val bloodGroups = listOf(
        "Select Blood Group",
        "A+", "A-",
        "B+", "B-",
        "O+", "O-",
        "AB+", "AB-",
        "Bombay Blood Group (hh)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBloodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.recyclerDonors.layoutManager = LinearLayoutManager(this)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bloodGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSearchBloodGroup.adapter = adapter

        binding.btnSearch.setOnClickListener {
            val selected = binding.spinnerSearchBloodGroup.selectedItem.toString()
            if (selected == "Select Blood Group") {
                Toast.makeText(this, "Please select a blood group first", Toast.LENGTH_SHORT).show()
            } else {
                if (!NetworkUtils.isOnline(this)) {

                    binding.tvResultStatus.text =
                        "⚠️ No internet connection. Please check your network."

                    return@setOnClickListener
                }

                searchDonors(selected)
            }
        }
    }

    private fun searchDonors(bloodGroup: String) {
        binding.tvResultStatus.text = "Searching for $bloodGroup donors..."

        db.collection("donors")
            .whereEqualTo("bloodGroup", bloodGroup)
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { documents, error ->

                if (error != null) {
                    binding.tvResultStatus.text =
                        "Error: ${error.message}"
                    return@addSnapshotListener
                }

                if (documents == null) {
                    binding.tvResultStatus.text =
                        "No data found"
                    return@addSnapshotListener
                }

                val allMatching = documents.map { doc ->
                    Donor(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        bloodGroup = doc.getString("bloodGroup") ?: "",
                        city = doc.getString("city") ?: "",
                        phone = doc.getString("phone") ?: "",
                        lastDonationDate = doc.getLong("lastDonationDate") ?: 0L,
                        isAvailable = doc.getBoolean("isAvailable") ?: true
                    )
                }

                // 90-day eligibility filter — core feature
                val eligible = allMatching.filter { it.isEligible() }

                if (eligible.isEmpty()) {

                    binding.tvResultStatus.text =
                        "No eligible $bloodGroup donors found"

                    binding.recyclerDonors.adapter = null

                } else {

                    binding.tvResultStatus.text =
                        "${eligible.size} eligible donor(s) found for $bloodGroup"

                    binding.recyclerDonors.adapter =
                        DonorAdapter(eligible)
                }
            }
    }
}