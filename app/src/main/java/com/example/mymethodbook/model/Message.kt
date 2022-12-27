package com.example.mymethodbook.model

data class Message(
    val to : String,
    val notification : Notification
)

data class Notification(
    val title : String,
    val body : String,
    val image : String?
)
