package com.example.data.model

data class UserProfile(
  val fullName: String = "",
  val age: Int = 25,
  val educationLevel: String = "Universitario",
  val gender: String = "No especificado",
  val isRegistered: Boolean = false
)
