package com.example.mymethodbook.model

data class Response(
    val message: String,
    val code: Int,
    val resultType: String, "result":
)

data class Movie(
    val id: Int,
    val title: String,
    val titleEng: String,
    val date: String,
    val userRating: Double,
    val audienceRating: Double,
    val reviewerRating: Double,
    val reservationRate: Double,
    val reservationGrade: Int,
    val grade: Int,
    val thumb: String
)