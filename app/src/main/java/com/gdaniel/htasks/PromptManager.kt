package com.gdaniel.htasks

import android.content.Context

object PromptManager {
    private const val MAX_FREE_PROMPTS = 15
    private const val PROMPTS_PER_PURCHASE = 30
    private const val PREFS_NAME = "prompt_prefs"
    private const val KEY_DAILY_COUNT = "dailyPromptCount"
    private const val KEY_PURCHASED = "purchasedPrompts"
    private const val KEY_LAST_RESET = "lastResetDate"

    fun getDailyPromptCount(context: Context): Int =
        prefs(context).getInt(KEY_DAILY_COUNT, 0)

    fun getPurchasedPrompts(context: Context): Int =
        prefs(context).getInt(KEY_PURCHASED, 0)

    fun canSendPrompt(context: Context): Boolean =
        getDailyPromptCount(context) < MAX_FREE_PROMPTS || getPurchasedPrompts(context) > 0

    fun remainingPrompts(context: Context): Int =
        if (getPurchasedPrompts(context) > 0) getPurchasedPrompts(context)
        else (MAX_FREE_PROMPTS - getDailyPromptCount(context)).coerceAtLeast(0)

    fun incrementPromptCount(context: Context) {
        val count = getDailyPromptCount(context) + 1
        prefs(context).edit().putInt(KEY_DAILY_COUNT, count).apply()
    }

    fun addPurchasedPrompts(context: Context) {
        val purchased = getPurchasedPrompts(context) + PROMPTS_PER_PURCHASE
        prefs(context).edit().putInt(KEY_PURCHASED, purchased).apply()
    }

    fun usePrompt(context: Context) {
        if (getPurchasedPrompts(context) > 0) {
            prefs(context).edit().putInt(KEY_PURCHASED, getPurchasedPrompts(context) - 1).apply()
        } else {
            incrementPromptCount(context)
        }
    }

    fun checkAndResetDailyCount(context: Context) {
        val prefs = prefs(context)
        val now = System.currentTimeMillis()
        val lastReset = prefs.getLong(KEY_LAST_RESET, 0L)
        val calendar = java.util.Calendar.getInstance()
        val lastCal = java.util.Calendar.getInstance().apply { timeInMillis = lastReset }
        if (!calendar.isSameDay(lastCal)) {
            prefs.edit().putInt(KEY_DAILY_COUNT, 0).putLong(KEY_LAST_RESET, now).apply()
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun java.util.Calendar.isSameDay(other: java.util.Calendar): Boolean =
        get(java.util.Calendar.YEAR) == other.get(java.util.Calendar.YEAR) &&
        get(java.util.Calendar.DAY_OF_YEAR) == other.get(java.util.Calendar.DAY_OF_YEAR)
} 