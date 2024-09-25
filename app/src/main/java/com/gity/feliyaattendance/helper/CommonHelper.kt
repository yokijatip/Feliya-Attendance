package com.gity.feliyaattendance.helper

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
}