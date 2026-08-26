package com.hospitalfinder.app.model

import com.hospitalfinder.app.model.schedule.HospitalSchedule

/**
 * Represents a single nearby hospital as shown in both the list and map
 * views, plus its operating schedule used by the hospital-details screen.
 */
data class Hospital(
    val id: String,
    val name: String,
    val isOpen: Boolean,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String? = null,
    val schedule: HospitalSchedule
)