package com.raktavahini.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raktavahini.app.databinding.ActivityRegisterDonorBinding

class RegisterDonorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterDonorBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedDateMillis: Long? = null

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
        binding = ActivityRegisterDonorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Spinner setup
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bloodGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBloodGroup.adapter = adapter

        binding.btnSubmitRegistration.setOnClickListener {
            registerDonor()
        }

        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }
        if (selectedDateMillis == null) {
            binding.btnSelectDate.text = "Never Donated (tap to change)"
        }
    }

    private fun registerDonor() {
        val name = binding.etName.text.toString().trim()
        val bloodGroup = binding.spinnerBloodGroup.selectedItem.toString()
        val city = binding.etCity.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim().filter { it.isDigit() }

        val uid = auth.currentUser?.uid ?: return

        if (name.isEmpty() || bloodGroup == "Select Blood Group" || city.isEmpty() || phone.length != 10) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val donor = hashMapOf(
            "name" to name,
            "bloodGroup" to bloodGroup,
            "city" to city,
            "phone" to phone,
            "lastDonationDate" to (selectedDateMillis ?: 0L),
            "isAvailable" to true,
            "donationHistory" to listOf<String>(),
        )

        db.collection("donors").document(uid)
            .set(donor, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register", Toast.LENGTH_SHORT).show()
            }
    }
    private fun showDatePicker() {
        val cal = java.util.Calendar.getInstance()

        val dialog = android.app.DatePickerDialog(this, { _, year, month, day ->

            val selected = java.util.Calendar.getInstance()
            selected.set(year, month, day, 0, 0, 0)

            selectedDateMillis = selected.timeInMillis

            binding.btnSelectDate.text = "$day/${month + 1}/$year"

        },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )

        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }
}