package com.raktavahini.app

/**
 * Donor data class — maps directly to a Firestore document.
 * ALL fields have defaults so Firestore can deserialize automatically.
 */
data class Donor(
    var id: String = "",               // Firestore document ID
    val name: String = "",
    val bloodGroup: String = "",       // e.g. "O+", "A-"
    val city: String = "",
    val phone: String = "",
    val lastDonationDate: Long = 0L,   // Stored as epoch millis; 0 = never donated
    var isAvailable: Boolean = true    // Donor availability toggle
) {
    /**
     * Core eligibility rule:
     * - Never donated (lastDonationDate == 0) → ELIGIBLE
     * - Donated more than 90 days ago → ELIGIBLE
     * - Otherwise → NOT eligible
     */
    fun isEligible(): Boolean {
        if (lastDonationDate == 0L) return true
        val daysSince = (System.currentTimeMillis() - lastDonationDate) /
                (1000L * 60 * 60 * 24)
        return daysSince > 90
    }

    /** Days remaining until donor becomes eligible again. 0 if already eligible. */
    fun daysUntilEligible(): Int {
        if (isEligible()) return 0
        val daysSince = (System.currentTimeMillis() - lastDonationDate) /
                (1000L * 60 * 60 * 24)
        return (90 - daysSince).toInt()
    }
}