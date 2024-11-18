package com.example.app_tentzo.pages

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import java.io.File
import java.io.FileOutputStream

// Funciones y estados principales de la pantalla del mapa
@Composable
fun MapaScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    var locationState by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var totalDistance by remember { mutableStateOf(0.0) }
    var isRouteActive by remember { mutableStateOf(false) }
    var isDistancesScreen by remember { mutableStateOf(false) }
    var distancesList by remember { mutableStateOf<List<Double>>(emptyList()) }
    var routesList by remember { mutableStateOf<List<Route>>(emptyList()) }




    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                startLocationUpdates(context, fusedLocationClient) { location ->
                    location?.let {
                        val newPoint = LatLng(it.latitude, it.longitude)
                        locationState = newPoint
                        if (isRouteActive) {
                            if (routePoints.isNotEmpty()) {
                                totalDistance += calculateDistance(routePoints.last(), newPoint)
                            }
                            routePoints = routePoints + newPoint
                        }
                    }
                }
            } else {
                Log.e("MapaScreen", "Permiso de ubicación denegado")
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
// Actualización de puntos y cálculo de distancia
            startLocationUpdates(context, fusedLocationClient) { location ->
                location?.let {
                    val newPoint = LatLng(it.latitude, it.longitude)
                    locationState = newPoint

                    // Verificar si la ruta está activa
                    if (isRouteActive) {
                        if (routePoints.isNotEmpty()) {
                            val lastPoint = routePoints.last()
                            val distance = calculateDistance(lastPoint, newPoint)
                            totalDistance += distance
                            Log.d("MapaScreen", "Distancia añadida: $distance metros. Total: $totalDistance metros")
                        }
                        routePoints = routePoints + newPoint
                        Log.d("MapaScreen", "Puntos de la ruta actualizados: ${routePoints.size}")
                    }
                }
            }

        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (isDistancesScreen) {
        DistancesScreen(routes = routesList, onBack = { isDistancesScreen = false })
    } else {
        locationState?.let { currentLocation ->
            MapScreen(
                currentLocation = currentLocation,
                routePoints = routePoints,
                totalDistance = totalDistance,
                isRouteActive = isRouteActive,
                onStartRoute = {
                    isRouteActive = true
                    routePoints = emptyList()
                    totalDistance = 0.0
                },
                onStopRoute = {
                    isRouteActive = false
                    saveDistanceToFirebase(uid, db, totalDistance, routePoints)
                },
                onViewDistances = {
                    loadRoutesFromFirebase(uid, db) { loadedRoutes ->
                        routesList = loadedRoutes
                        isDistancesScreen = true
                    }
                }
            )
        } ?: LoadingScreen()
    }


}

// Pantalla principal del mapa con controles
@Composable
fun MapScreen(
    currentLocation: LatLng,
    routePoints: List<LatLng>,
    totalDistance: Double,
    isRouteActive: Boolean,
    onStartRoute: () -> Unit,
    onStopRoute: () -> Unit,
    onViewDistances: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = currentLocation),
                title = "Ubicación Actual"
            )

// Dibujar círculos en el mapa
            routePoints.forEach { point ->
                Log.d("MapaScreen", "Dibujando círculo en: ${point.latitude}, ${point.longitude}")
                Circle(
                    center = point,
                    radius = 10.0,
                    strokeColor = Color.Red,
                    fillColor = Color(0xAAFF0000)
                )
            }

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Distancia recorrida: ${"%.2f".format(totalDistance / 1000)} km",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent) // Asegúrate de que no haya fondo
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly

            ) {
                Button(
                    onClick = onStartRoute,
                    enabled = !isRouteActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF247E3D), // Color #247E3D
                        contentColor = Color.White // Color del texto
                    )
                ) {
                    Text("Iniciar Ruta")
                }
                Button(
                    onClick = onStopRoute,
                    enabled = isRouteActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF247E3D),
                        contentColor = Color.White
                    )
                ) {
                    Text("Terminar Ruta")
                }
                Button(
                    onClick = onViewDistances,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF247E3D),
                        contentColor = Color.White
                    )
                ) {
                    Text("Ver Rutas guardadas")
                }
            }
        }
    }
}


@Composable
fun DistancesScreen(routes: List<Route>, onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Fondo gris claro para la pantalla
            .padding(16.dp)
    ) {
        // Botón de regreso en la parte superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.wrapContentSize(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF247E3D),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp) // Esquinas redondeadas
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    modifier = Modifier.size(24.dp) // Tamaño del ícono
                )
            }
            Spacer(modifier = Modifier.width(8.dp)) // Espaciado entre el botón y el título
            Text(
                text = "Rutas Guardadas",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = Color(0xFF247E3D) // Verde para el texto del título
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f), // Ocupa el espacio restante para desplazamiento
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espaciado entre elementos
        ) {
            items(routes) { route ->
                // Variable googleMap definida para cada ruta
                var googleMap: GoogleMap? by remember { mutableStateOf(null) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)), // Sombra y bordes redondeados
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column {
                        // Fila Superior
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Distancia
                            Text(
                                text = "Distancia: ${"%.2f".format(route.totalDistance / 1000)} km",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            // Botón de compartir
                            IconButton(
                                onClick = {
                                    googleMap?.let { map ->
                                        takeSnapshotAndShare(context, map, route.totalDistance)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartir",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color(0xFF247E3D) // Verde para el ícono de compartir
                                )
                            }
                        }

                        // Mapa
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.White)
                                .clip(RoundedCornerShape(12.dp)) // Bordes redondeados
                                .padding(8.dp)
                        ) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { mapViewContext ->
                                    MapView(mapViewContext).apply {
                                        onCreate(null)
                                        getMapAsync { map ->
                                            googleMap = map
                                            val latLngPoints = route.routePoints.map { LatLng(it.latitude, it.longitude) }
                                            val polylineOptions = PolylineOptions().addAll(latLngPoints).color(android.graphics.Color.BLUE).width(5f)
                                            map.addPolyline(polylineOptions)

                                            // Centrar cámara
                                            if (latLngPoints.isNotEmpty()) {
                                                val boundsBuilder = LatLngBounds.builder()
                                                latLngPoints.forEach { boundsBuilder.include(it) }
                                                map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 50))
                                            }
                                        }
                                    }
                                },
                                update = { mapView ->
                                    mapView.onResume()
                                }
                            )
                        }
                    }
                }
            }
            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}


// Función para tomar una captura de pantalla del mapa y compartirla
fun takeSnapshotAndShare(context: Context, map: GoogleMap, distance: Double) {
    map.snapshot { bitmap ->
        if (bitmap != null) {
            // Guardar el bitmap en un archivo temporal
            val file = File(context.cacheDir, "map_snapshot.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }

            // Crear un URI para el archivo
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Mensaje con la distancia
            val distanceText = "Distancia recorrida: ${"%.2f".format(distance / 1000)} km"

            // Intent de compartir
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, distanceText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Lanzar el intent de compartir
            context.startActivity(Intent.createChooser(shareIntent, "Compartir mapa y distancia"))
        } else {
            Log.e("ShareMap", "La captura de pantalla del mapa falló.")
        }
    }
}

// Pantalla de carga
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("Cargando mapa...", color = Color.Gray)
    }
}

// Funciones auxiliares
fun startLocationUpdates(
    context: android.content.Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationUpdated: (Location?) -> Unit
) {
    val locationRequest = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 2000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { onLocationUpdated(it) }
        }
    }

    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    } else {
        Log.e("MapaScreen", "Permiso de ubicación no concedido")
    }
}

fun calculateDistance(start: LatLng, end: LatLng): Double {
    val results = FloatArray(1)
    Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        results
    )
    return results[0].toDouble()
}

fun saveDistanceToFirebase(uid: String, db: FirebaseFirestore, distance: Double, routePoints: List<LatLng>) {
    // Convertir los routePoints a un formato adecuado para Firebase
    val points = routePoints.map { mapOf("latitude" to it.latitude, "longitude" to it.longitude) }
    val data = mapOf(
        "distance" to distance,
        "routePoints" to points // Guardar los puntos de la ruta
    )

    db.collection("users").document(uid).collection("routes")
        .add(data)
        .addOnSuccessListener { Log.d("MapaScreen", "Distancia y ruta guardadas exitosamente") }
        .addOnFailureListener { Log.e("MapaScreen", "Error al guardar la distancia y la ruta", it) }
}


fun loadRoutesFromFirebase(
    uid: String,
    db: FirebaseFirestore,
    onRoutesLoaded: (List<Route>) -> Unit
) {
    db.collection("users").document(uid).collection("routes")
        .get()
        .addOnSuccessListener { documents ->
            val routes = documents.mapNotNull { document ->
                val points = document.get("routePoints") as? List<*>
                val distance = document.getDouble("distance") ?: 0.0

                // Mapear puntos a LocationData
                val routePoints = points?.mapNotNull { point ->
                    (point as? Map<*, *>)?.let { map ->
                        val lat = map["latitude"] as? Double
                        val lng = map["longitude"] as? Double
                        if (lat != null && lng != null) LocationData(lat, lng) else null
                    }
                }

                // Crear un objeto Route si los puntos son válidos
                if (routePoints != null && routePoints.isNotEmpty()) {
                    Route(routePoints = routePoints, totalDistance = distance)
                } else {
                    null
                }
            }
            onRoutesLoaded(routes)
        }
        .addOnFailureListener { e ->
            Log.e("MapaScreen", "Error al cargar rutas desde Firebase", e)
        }
}


