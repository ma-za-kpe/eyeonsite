package com.maku.eyeonsite.admin

data class WaitlistDocument(
    val name: String,
    val email: String,
    val role: String,
    val location: String,
    val message: String,
    val createdAt: String,
)
