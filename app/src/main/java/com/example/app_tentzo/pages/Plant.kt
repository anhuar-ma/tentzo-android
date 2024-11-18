package com.example.app_tentzo.pages



data class Plant(
    val imageUrl: String = "",    // URL for the plant image
    val name: String = "",        // Plant name
    val scientificName: String = "", // Scientific name
    val description: String = "", // Description of the plant
    val source: String = ""       // Reference or source link
)

