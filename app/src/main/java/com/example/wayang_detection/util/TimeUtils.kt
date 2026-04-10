package com.example.wayang_detection.util

import java.util.Calendar

/**
 * Utility for time-based greeting messages.
 */
object TimeUtils {
    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..17 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    fun getGreetingEmoji(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> "☀️"
            in 11..14 -> "🌤️"
            in 15..17 -> "🌅"
            else -> "🌙"
        }
    }
}
