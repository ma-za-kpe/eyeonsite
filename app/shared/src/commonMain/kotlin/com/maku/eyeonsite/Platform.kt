package com.maku.eyeonsite

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
