package com.fontys.frontend.utils

import android.content.Context
import android.content.SharedPreferences


object ChallengePreferences {
    private const val PREFS_NAME = "challenge_prefs"
    private const val KEY_CHALLENGE_START_TIME = "challenge_start_time"
    private const val KEY_CHALLENGE_ID = "challenge_id"

    private var prefs: SharedPreferences? = null


    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }


    fun saveChallengeStartData(startTimeMillis: Long, challengeId: Int) {
        prefs?.edit()?.apply {
            putLong(KEY_CHALLENGE_START_TIME, startTimeMillis)
            putInt(KEY_CHALLENGE_ID, challengeId)
            apply()
        }
    }


    fun getChallengeStartTime(): Long {
        return prefs?.getLong(KEY_CHALLENGE_START_TIME, 0L) ?: 0L
    }

    fun getChallengeId(): Int {
        return prefs?.getInt(KEY_CHALLENGE_ID, -1) ?: -1
    }


    fun clearChallengeData() {
        prefs?.edit()?.apply {
            remove(KEY_CHALLENGE_START_TIME)
            remove(KEY_CHALLENGE_ID)
            apply()
        }
    }

    fun hasSavedChallenge(): Boolean {
        val startTime = getChallengeStartTime()
        val challengeId = getChallengeId()
        return startTime > 0L && challengeId > -1
    }
}

