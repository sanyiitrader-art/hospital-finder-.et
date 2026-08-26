package com.hospitalfinder.app.model.schedule

import java.time.LocalTime

/**
 * A hospital's configured operating schedule. This is local/demo data for
 * now, but the shape is intended to match what a real backend would
 * eventually supply — nothing here is UI-specific.
 *
 * closesAt may be numerically earlier than opensAt (e.g. opens 2:30 PM,
 * closes 12:30 AM) — that represents an overnight operating period, not
 * an error. See [OperatingStateCalculator] for how that's interpreted.
 *
 * lunchStart/lunchEnd are both null when no lunch/service break is
 * configured for this hospital; a break is only considered configured
 * when both are present.
 */
data class HospitalSchedule(
    val opensAt: LocalTime,
    val closesAt: LocalTime,
    val lunchStart: LocalTime? = null,
    val lunchEnd: LocalTime? = null,
    val ticketsRemainingToday: Int
)