package com.example.mymethodbook.model

data class TestResponse(
    val message: String,
    val code: Int,
    val resultType: String,
    val result: List<Movie>
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