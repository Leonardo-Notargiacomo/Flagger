package com.fontys.frontend.utils

import android.content.Context
import android.content.SharedPreferences

object OnboardingPreferences {
    private const val PREFS_NAME = "flagger_onboarding_prefs"
    private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun hasSeenOnboarding(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
    }

    fun setOnboardingSeen(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_HAS_SEEN_ONBOARDING, true)
            .apply()
    }

    // For development/testing - reset onboarding
    fun resetOnboarding(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_HAS_SEEN_ONBOARDING, false)
            .apply()
    }
}
