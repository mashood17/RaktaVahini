package com.raktavahini.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.raktavahini.app.databinding.ActivityRegisterDonorBinding
import java.util.*

class RegisterDonorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterDonorBinding
    private val db = FirebaseFirestore.getInstance()
    private var selectedDateMillis: Long? = null

    private val bloodGroups = listOf(
        "Select Blood Group", "A+", "A-", "B+", "B-",
        "O+", "O-", "AB+", "AB-"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterDonorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Blood group spinner setup
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bloodGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBloodGroup.adapter = adapter

        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSubmitRegistration.setOnClickListener { submitRegistration() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selected = Calendar.getInstance()
            selected.set(year, month, day, 0, 0, 0)
            selectedDateMillis = selected.timeInMillis
            binding.btnSelectDate.text = "$day/${month+1}/$year"
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun submitRegistration() {
        val name = binding.etName.text.toString().trim()
        val bloodGroup = binding.spinnerBloodGroup.selectedItem.toString()
        val city = binding.etCity.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty()) { showError("Please enter your name"); return }
        if (bloodGroup == "Select Blood Group") { showError("Please select a blood group"); return }
        if (city.isEmpty()) { showError("Please enter your city"); return }
        if (phone.length != 10) { showError("Enter a valid 10-digit number"); return }

        binding.btnSubmitRegistration.isEnabled = false
        binding.btnSubmitRegistration.text = "Registering..."

        // Check for duplicate phone
        db.collection("donors").whereEqualTo("phone", phone).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    showError("This phone number is already registered")
                    binding.btnSubmitRegistration.isEnabled = true
                    binding.btnSubmitRegistration.text = "Register as Donor"
                } else {
                    saveDonorToFirestore(name, bloodGroup, city, phone)
                }
            }
            .addOnFailureListener {
                showError("Network error. Check your connection.")
                binding.btnSubmitRegistration.isEnabled = true
                binding.btnSubmitRegistration.text = "Register as Donor"
            }
    }

    private fun saveDonorToFirestore(name: String, bloodGroup: String, city: String, phone: String) {
        val donor = hashMapOf(
            "name" to name, "bloodGroup" to bloodGroup,
            "city" to city, "phone" to phone,
            "lastDonationDate" to (selectedDateMillis ?: 0L),
            "isAvailable" to true
        )
        db.collection("donors").add(donor)
            .addOnSuccessListener { docRef ->
                // Save donorId locally so they can access their profile later
                getSharedPreferences("RaktaVahini", MODE_PRIVATE).edit()
                    .putString("donorId", docRef.id)
                    .putString("donorPhone", phone).apply()
                Toast.makeText(this, "✅ Registered! Thank you.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                showError("Failed: ${e.message}")
                binding.btnSubmitRegistration.isEnabled = true
                binding.btnSubmitRegistration.text = "Register as Donor"
            }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.blood_red))
            .setTextColor(getColor(R.color.white)).show()
    }
}