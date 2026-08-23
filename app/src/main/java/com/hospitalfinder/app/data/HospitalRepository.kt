package com.hospitalfinder.app.data

import com.hospitalfinder.app.model.Hospital

/**
 * Provides the list of nearby hospitals.
 *
 * This is a static, in-memory placeholder source. There is no backend,
 * database, or network fetch at this stage of the app per current scope —
 * only the UI foundation for displaying hospitals is implemented here.
 * Replacing this with a real data source later does not require any
 * change to the UI layer, since fragments only depend on this interface.
 */
object HospitalRepository {

    fun getNearbyHospitals(): List<Hospital> = listOf(
        Hospital(
            id = "1",
            name = "St. Mary General Hospital",
            isOpen = true,
            latitude = 9.0300,
            longitude = 38.7400
        ),
        Hospital(
            id = "2",
            name = "Riverside Medical Center",
            isOpen = false,
            latitude = 9.0180,
            longitude = 38.7520
        ),
        Hospital(
            id = "3",
            name = "Northgate Community Hospital",
            isOpen = false,
            latitude = 9.0410,
            longitude = 38.7290
        ),
        Hospital(
            id = "4",
            name = "Unity Health Clinic",
            isOpen = false,
            latitude = 9.0090,
            longitude = 38.7610
        ),
        Hospital(
            id = "5",
            name = "Green Valley Hospital",
            isOpen = true,
            latitude = 9.0470,
            longitude = 38.7450
        )
    )
}