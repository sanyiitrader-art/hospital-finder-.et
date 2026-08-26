package com.hospitalfinder.app.model.schedule

import java.time.Duration
import java.time.LocalTime

/**
 * Derives the current [HospitalOperatingState] from a [HospitalSchedule]
 * and the current time. Contains the only real logic in this feature —
 * in particular, correct handling of overnight schedules (where closesAt
 * is numerically earlier than opensAt) and lunch-window detection.
 *
 * This is a pure function deliberately kept separate from any UI or
 * Android framework class so it can be unit-tested in isolation and so
 * the same logic can later be replaced by a direct server-computed state
 * without touching the calculation shape used by the UI.
 */
object OperatingStateCalculator {

    fun calculate(schedule: HospitalSchedule, now: LocalTime): HospitalOperatingState {
        if (!isWithinOperatingWindow(schedule.opensAt, schedule.closesAt, now)) {
            return HospitalOperatingState.Closed(opensAt = schedule.opensAt)
        }

        val lunchStart = schedule.lunchStart
        val lunchEnd = schedule.lunchEnd
        if (lunchStart != null && lunchEnd != null &&
            isWithinOperatingWindow(lunchStart, lunchEnd, now)
        ) {
            return HospitalOperatingState.OnLunchBreak(resumesAt = lunchEnd)
        }

        val timeUntilClose = durationUntil(now, schedule.closesAt)

        return if (schedule.ticketsRemainingToday <= 0) {
            HospitalOperatingState.TicketsFull(timeUntilClose = timeUntilClose)
        } else {
            HospitalOperatingState.Open(
                ticketsLeft = schedule.ticketsRemainingToday,
                timeUntilClose = timeUntilClose
            )
        }
    }

    /**
     * True if [now] falls within [start]..[end], correctly handling the
     * case where [end] is numerically earlier than [start] (an overnight
     * window that crosses midnight).
     */
    private fun isWithinOperatingWindow(start: LocalTime, end: LocalTime, now: LocalTime): Boolean {
        return if (end.isAfter(start)) {
            // Same-day window, e.g. 09:00–17:00.
            !now.isBefore(start) && now.isBefore(end)
        } else {
            // Overnight window, e.g. 14:30–00:30 (crosses midnight).
            !now.isBefore(start) || now.isBefore(end)
        }
    }

    /**
     * Duration from [now] until [target], correctly wrapping past
     * midnight when [target] is numerically earlier than [now] (meaning
     * it falls on the following calendar day).
     */
    private fun durationUntil(now: LocalTime, target: LocalTime): Duration {
        return if (target.isAfter(now)) {
            Duration.between(now, target)
        } else {
            Duration.between(now, LocalTime.MAX).plusSeconds(1).plus(Duration.between(LocalTime.MIN, target))
        }
    }
}