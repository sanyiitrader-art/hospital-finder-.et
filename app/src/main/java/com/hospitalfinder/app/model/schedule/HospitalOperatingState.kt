package com.hospitalfinder.app.model.schedule

import java.time.Duration
import java.time.LocalTime

/**
 * The four patient-facing states the hospital-details screen must be able
 * to represent. Each state carries only the data relevant to it, so the
 * UI layer renders by branching on this type rather than on raw
 * booleans/numbers scattered across the schedule.
 *
 * This is a live snapshot only — see [OperatingStateCalculator]. It is
 * never treated as a reservation or a guarantee of ticket availability;
 * the eventual backend remains the source of truth for that.
 */
sealed class HospitalOperatingState {

    /** Open, tickets available, GET is actionable. */
    data class Open(
        val ticketsLeft: Int,
        val timeUntilClose: Duration
    ) : HospitalOperatingState()

    /** Open overall, but currently within a configured lunch/service break. */
    data class OnLunchBreak(
        val resumesAt: LocalTime
    ) : HospitalOperatingState()

    /** Open, but today's tickets have all been allocated. */
    data class TicketsFull(
        val timeUntilClose: Duration
    ) : HospitalOperatingState()

    /** Not currently operating for the day. */
    data class Closed(
        val opensAt: LocalTime
    ) : HospitalOperatingState()
}