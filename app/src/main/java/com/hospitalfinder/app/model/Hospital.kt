package com.hospitalfinder.app.model

/**
 * Represents a single nearby hospital as shown in both the list and map views.
 * Fields are intentionally minimal per current scope — no queue data,
 * no pricing, no contact info beyond what the reference design shows.
 */
data class Hospital(
    val id: String,
    val name: String,
    val isOpen: Boolean,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String? = null
)