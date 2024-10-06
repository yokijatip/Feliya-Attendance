package com.gity.feliyaattendance.helper

import android.content.Context
import android.icu.util.Calendar
import android.view.View
import android.widget.ProgressBar
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

object CommonHelper {
    fun generateRandomEmail(): String {
        val random = Random
        val domains = listOf("gmail.com", "yahoo.com", "outlook.com")
        val randomDomain = domains.random()
        val randomString = (1..10).map { ('a'..'z').random() }.joinToString("")
        return "${randomString}@${randomDomain}"
    }

    fun generateRandomPassword(length: Int = 8): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length).map { allowedChars.random() }.joinToString("")
    }

    fun getCurrentDayOnly(): String {
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayOfWeek = dateFormat.format(currentDate)
        return dayOfWeek
    }

    fun getCurrentDateOnly(): String {
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val dayOfWeek = dateFormat.format(currentDate)
        return dayOfWeek
    }

    fun getGreetingsMessage(
        goodMorning: String,
        goodAfternoon: String,
        goodEvening: String
    ): String {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hourOfDay) {
            in 0..11 -> goodMorning
            in 12..16 -> goodAfternoon
            else -> goodEvening

        }
        return greeting
    }

    fun formatTimestamp(timestamp: Timestamp?): String {
        return timestamp?.toDate()?.let {
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            dateFormat.format(it)
        } ?: "Tanggal tidak tersedia"
    }

    fun showLoading(context: Context, loadingBar: ProgressBar, loadingOverlay: View) {
        loadingBar.visibility = View.VISIBLE
        loadingOverlay.visibility = View.VISIBLE
        // Jika Anda ingin gelap, Anda bisa mengatur warna di View
        loadingOverlay.alpha = 0.5f // Sesuaikan transparansi sesuai kebutuhan
    }

    fun hideLoading(loadingBar: ProgressBar, loadingOverlay: View) {
        loadingBar.visibility = View.GONE
        loadingOverlay.visibility = View.GONE
    }


}