package com.maku.eyeonsite.domain

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
)
