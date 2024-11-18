package com.example.app_tentzo.pages


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavedRoutesScreen : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var savedRoutes by mutableStateOf<List<Route>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchSavedRoutes()

        setContent {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Rutas Guardadas",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(savedRoutes) { route ->
                        RouteItem(route)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { finish() }) {
                    Text("Volver")
                }
            }
        }
    }

    private fun fetchSavedRoutes() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.e("Firebase", "Usuario no autenticado")
            return
        }

        db.collection("users").document(uid).collection("routes")
            .get()
            .addOnSuccessListener { documents ->
                savedRoutes = documents.mapNotNull { document ->
                    val points = document.get("routePoints") as? List<*>
                    val distance = document.getDouble("totalDistance") ?: 0.0
                    if (points is List<*>) {
                        val locationDataPoints = points.mapNotNull { point ->
                            (point as? Map<*, *>)?.let {
                                val lat = it["latitude"] as? Double ?: 0.0
                                val lng = it["longitude"] as? Double ?: 0.0
                                LocationData(lat, lng)
                            }
                        }
                        Route(routePoints = locationDataPoints, totalDistance = distance)
                    } else {
                        null
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Error al obtener las rutas: $e")
            }
    }

}

@Composable
fun RouteItem(route: Route) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Distancia: ${"%.2f".format(route.totalDistance / 1000)} km")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* Aquí puedes agregar la lógica para ver la ruta en el mapa */ }) {
            Text("Ver Ruta")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
