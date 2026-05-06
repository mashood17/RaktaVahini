package com.raktavahini.app

import com.google.firebase.firestore.PropertyName

/**
 * Donor data class — maps to Firestore document.
 * Using @PropertyName to handle Firestore field name mapping explicitly.
 * All fields have defaults so Firestore can auto-deserialize.
 */
data class Donor(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("bloodGroup") @set:PropertyName("bloodGroup")
    var bloodGroup: String = "",

    @get:PropertyName("city") @set:PropertyName("city")
    var city: String = "",

    @get:PropertyName("phone") @set:PropertyName("phone")
    var phone: String = "",

    @get:PropertyName("lastDonationDate") @set:PropertyName("lastDonationDate")
    var lastDonationDate: Long = 0L,

    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true
) {
    /**
     * Core eligibility rule:
     * - Never donated (lastDonationDate == 0) → ELIGIBLE
     * - Donated > 90 days ago → ELIGIBLE
     * - Otherwise → NOT eligible
     */
    fun isEligible(): Boolean {
        if (lastDonationDate == 0L) return true
        val daysSince = (System.currentTimeMillis() - lastDonationDate) /
                (1000L * 60 * 60 * 24)
        return daysSince > 90
    }

    /** Days remaining until next eligible. 0 if already eligible. */
    fun daysUntilEligible(): Int {
        if (isEligible()) return 0
        val daysSince = (System.currentTimeMillis() - lastDonationDate) /
                (1000L * 60 * 60 * 24)
        return (90 - daysSince).toInt()
    }
}