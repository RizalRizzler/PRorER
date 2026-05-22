package com.example.prorer.ui.shared

import android.content.Context
import androidx.preference.PreferenceManager
import kotlin.math.roundToInt

object UnitFormatter {
    fun isLbs(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("unit_preference", "kg") == "lbs"
    }

    fun displayWeight(weightKg: Float, isLbs: Boolean): String {
        if (weightKg <= 0f) return ""
        return if (isLbs) {
            val lbs = weightKg * 2.20462f //conversion + snap to 0.5 increments

            val snappedLbs = (lbs * 2).roundToInt() / 2.0f
            String.format("%.1f", snappedLbs).removeSuffix(".0")
        } else {
            String.format("%.1f", weightKg).removeSuffix(".0")
        }
    }

    fun parseInputToKg(input: String, isLbs: Boolean): Float {
        val rawValue = input.toFloatOrNull() ?: return 0f
        return if (isLbs) {
            rawValue / 2.20462f
        } else {
            rawValue
        }
    }
}