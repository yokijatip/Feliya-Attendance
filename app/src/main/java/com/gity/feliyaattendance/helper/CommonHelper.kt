package com.gity.feliyaattendance.helper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StyleRes
import com.gity.feliyaattendance.databinding.CustomDialogConfimationBinding
import com.gity.feliyaattendance.databinding.CustomDialogInformationFailedBinding
import com.gity.feliyaattendance.databinding.CustomDialogInformationSuccessBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
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

    fun formatTimeOnly(timestamp: Timestamp?): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp?.toDate() ?: "00:00")
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

    // Fungsi untuk mengubah string ke Timestamp Firestore
    fun stringToTimestamp(dateString: String): Timestamp? {
        return try {
            val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val date: Date? = format.parse(dateString) // Konversi String ke Date
            Timestamp(date!!) // Konversi Date ke Timestamp Firestore
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun stringToDate(dateString: String): Date? {
        return try {
            val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            format.parse(dateString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun showConfirmationDialog(
        context: Context,
        title: String,
        description: String,
        positiveButtonText: String = "Ya",
        negativeButtonText: String = "Tidak",
        onPositiveClick: () -> Unit = {},
        onNegativeClick: () -> Unit = {},
        cancelable: Boolean = true,
        @StyleRes themeResId: Int = 0
    ) {
        val dialog = if (themeResId != 0) Dialog(context, themeResId) else Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = CustomDialogConfimationBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(binding) {
            tvTitle.text = title
            tvDescription.text = description
            tvYes.text = positiveButtonText
            tvNo.text = negativeButtonText

            btnYes.setOnClickListener {
                onPositiveClick()
                dialog.dismiss()
            }
            btnNo.setOnClickListener {
                onNegativeClick()
                dialog.dismiss()
            }
        }
        dialog.setCancelable(cancelable)
        dialog.show()
    }

    fun showInformationSuccessDialog(
        context: Context,
        title: String,
        description: String,
        okButtonText: String = "OK",
        onOkButton: () -> Unit = {},
        cancelable: Boolean = true,
        @StyleRes themeResId: Int = 0
    ) {
        val dialog = if (themeResId != 0) Dialog(context, themeResId) else Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = CustomDialogInformationSuccessBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(binding) {
            tvTitle.text = title
            tvDescription.text = description
            tvOk.text = okButtonText

            btnOk.setOnClickListener {
                onOkButton()
                dialog.dismiss()
            }
        }
        dialog.setCancelable(cancelable)
        dialog.show()
    }

    fun showInformationFailedDialog(
        context: Context,
        title: String,
        description: String,
        okButtonText: String = "OK",
        onOkButton: () -> Unit = {},
        cancelable: Boolean = true,
        @StyleRes themeResId: Int = 0
    ) {
        val dialog = if (themeResId != 0) Dialog(context, themeResId) else Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = CustomDialogInformationFailedBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(binding) {
            tvTitle.text = title
            tvDescription.text = description
            tvOk.text = okButtonText

            btnOk.setOnClickListener {
                onOkButton()
                dialog.dismiss()
            }
        }
        dialog.setCancelable(cancelable)
        dialog.show()
    }

    // Extension function untuk membuat Timestamp dari Date
    fun Date.toTimestamp(): Timestamp {
        return Timestamp(this)
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


}