package com.hospitalfinder.app.data

import com.hospitalfinder.app.model.Hospital
import com.hospitalfinder.app.model.schedule.HospitalSchedule
import java.time.LocalTime

/**
 * Provides the list of nearby hospitals.
 *
 * This is a static, in-memory placeholder source. There is no backend,
 * database, or network fetch at this stage of the app per current scope —
 * only the UI foundation for displaying hospitals is implemented here.
 * Replacing this with a real data source later does not require any
 * change to the UI layer, since fragments/activities only depend on this
 * object's shape.
 *
 * Schedules below are deliberately varied (with lunch, without lunch,
 * overnight hours, zero tickets, currently closed) so every
 * HospitalOperatingState is reachable from this demo data.
 */
object HospitalRepository {

    fun getNearbyHospitals(): List<Hospital> = listOf(
        Hospital(
            id = "1",
            name = "St. Mary General Hospital",
            isOpen = true,
            latitude = 9.0300,
            longitude = 38.7400,
            schedule = HospitalSchedule(
                opensAt = LocalTime.of(8, 0),
                closesAt = LocalTime.of(20, 0),
                lunchStart = LocalTime.of(13, 0),
                lunchEnd = LocalTime.of(14, 0),
                ticketsRemainingToday = 30
            )
        ),
        Hospital(
            id = "2",
            name = "Riverside Medical Center",
            isOpen = false,
            latitude = 9.0180,
            longitude = 38.7520,
            schedule = HospitalSchedule(
                opensAt = LocalTime.of(9, 0),
                closesAt = LocalTime.of(17, 0),
                ticketsRemainingToday = 0
            )
        ),
        Hospital(
            id = "3",
            name = "Northgate Community Hospital",
            isOpen = false,
            latitude = 9.0410,
            longitude = 38.7290,
            schedule = HospitalSchedule(
                opensAt = LocalTime.of(14, 30),
                closesAt = LocalTime.of(0, 30),
                ticketsRemainingToday = 12
            )
        ),
        Hospital(
            id = "4",
            name = "Unity Health Clinic",
            isOpen = false,
            latitude = 9.0090,
            longitude = 38.7610,
            schedule = HospitalSchedule(
                opensAt = LocalTime.of(7, 0),
                closesAt = LocalTime.of(15, 0),
                ticketsRemainingToday = 5
            )
        ),
        Hospital(
            id = "5",
            name = "Green Valley Hospital",
            isOpen = true,
            latitude = 9.0470,
            longitude = 38.7450,
            schedule = HospitalSchedule(
                opensAt = LocalTime.of(0, 0),
                closesAt = LocalTime.of(23, 59),
                ticketsRemainingToday = 8
            )
        )
    )

    fun getById(id: String): Hospital? =
        getNearbyHospitals().firstOrNull { it.id == id }
}