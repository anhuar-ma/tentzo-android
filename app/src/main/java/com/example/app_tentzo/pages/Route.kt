package com.example.app_tentzo.pages

// Clase Route
data class Route(
    val routePoints: List<LocationData> = emptyList(),
    val totalDistance: Double = 0.0
) {
    constructor() : this(emptyList(), 0.0)
}
