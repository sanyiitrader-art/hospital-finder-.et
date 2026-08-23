package com.hospitalfinder.app.util

import android.view.View

/**
 * Small, dependency-free view helpers used across the UI layer.
 * Kept intentionally minimal to avoid pulling in a general-purpose
 * "extensions" library just for a couple of one-line helpers.
 */

fun View.setSelectedState(isSelected: Boolean) {
    this.isSelected = isSelected
    // The active-indicator background (bg_active_indicator.xml) only
    // draws a visible stroke for the selected state; for the unselected
    // state we hide the stroke by making the background transparent.
    this.background.alpha = if (isSelected) 255 else 0
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}