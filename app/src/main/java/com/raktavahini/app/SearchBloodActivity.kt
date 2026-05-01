package com.raktavahini.app

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.raktavahini.app.databinding.ActivitySearchBloodBinding

class SearchBloodActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBloodBinding
    private val db = FirebaseFirestore.getInstance()
    private val bloodGroups = listOf(
        "Select Blood Group", "A+", "A-", "B+", "B-",
        "O+", "O-", "AB+", "AB-"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBloodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerDonors.layoutManager = LinearLayoutManager(this)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bloodGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSearchBloodGroup.adapter = adapter

        binding.spinnerSearchBloodGroup.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selected = bloodGroups[position]
                    if (selected != "Select Blood Group") {
                        searchDonors(selected)
                    } else {
                        binding.tvResultStatus.text = "Select a blood group to find donors"
                        binding.recyclerDonors.adapter = null
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun searchDonors(bloodGroup: String) {
        binding.tvResultStatus.text = "Searching..."

        db.collection("donors")
            .whereEqualTo("bloodGroup", bloodGroup)
            .whereEqualTo("isAvailable", true)
            .get()
            .addOnSuccessListener { documents ->
                val allMatching = documents.mapNotNull { doc ->
                    val donor = doc.toObject(Donor::class.java)
                    donor.id = doc.id
                    donor
                }

                // 90-day eligibility filter — the heart of the app
                val eligible = allMatching.filter { it.isEligible() }

                if (eligible.isEmpty()) {
                    binding.tvResultStatus.text = "No eligible $bloodGroup donors found"
                    binding.recyclerDonors.adapter = null
                } else {
                    binding.tvResultStatus.text =
                        "${eligible.size} eligible donor(s) found for $bloodGroup"
                    binding.recyclerDonors.adapter = DonorAdapter(eligible)
                }
            }
            .addOnFailureListener {
                binding.tvResultStatus.text = "Network error. Check your connection."
            }
    }
}