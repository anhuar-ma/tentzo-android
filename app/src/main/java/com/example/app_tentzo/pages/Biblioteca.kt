package com.example.app_tentzo.pages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

import com.example.app_tentzo.pages.Plant
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.squareup.picasso.Picasso
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app_tentzo.R

@Composable
fun Biblioteca() {
    // Punto de entrada para Compose
    AppTentzoTheme{
        val navController = rememberNavController()
        val database = Firebase.database.reference

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            var plants by remember { mutableStateOf(emptyList<Plant>()) }

            // Cargar datos desde Firebase
            loadPlantsFromRealtimeDatabase(database) { loadedPlants ->
                plants = loadedPlants
            }

            // Configurar el NavHost
            NavHost(
                navController = navController,
                startDestination = "plantList",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("plantList") {
                    PlantListScreen(
                        plantList = plants,
                        onPlantClick = { plantName -> navController.navigate("preview/$plantName") }
                    )
                }
                composable("preview/{plantName}") { backStackEntry ->
                    val plantName = backStackEntry.arguments?.getString("plantName")
                    val plant = plants.firstOrNull { it.name == plantName }
                    plant?.let {
                        Preview(plant = it, navController = navController)
                    }
                }
            }
        }
    }
}

fun loadPlantsFromRealtimeDatabase(database: DatabaseReference, onPlantsLoaded: (List<Plant>) -> Unit) {
    val plantsRef = database.child("plants")

    plantsRef.get().addOnSuccessListener { snapshot ->
        val plantList = snapshot.children.mapNotNull { it.getValue<Plant>() }
        onPlantsLoaded(plantList)
    }

    plantsRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val plantList = dataSnapshot.children.mapNotNull { it.getValue<Plant>() }
            onPlantsLoaded(plantList)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Manejar errores
        }
    })
}

@Composable
fun PlantListScreen(plantList: List<Plant>, onPlantClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFD3D3D3))) {

        Text(
            text = "Biblioteca de flora",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        )

        // TextField para búsqueda
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar planta") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(7.dp)
        )

        // Filtrar las plantas por el nombre que contiene el texto de búsqueda
        val filteredPlants = if (searchQuery.isEmpty()) {
            plantList
        } else {
            plantList.filter { plant ->
                plant.name.contains(searchQuery, ignoreCase = true)
            }
        }

        // Mostrar la lista filtrada de plantas o un mensaje de error si no hay coincidencias
        if (filteredPlants.isEmpty()) {
            // Si no hay plantas que coincidan, mostrar mensaje de error
            Text(
                text = "No se encontraron plantas que coincidan con '$searchQuery'.",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Si hay plantas que coinciden, mostrar la lista
            PlantList(
                plantList = filteredPlants,
                onPlantClick = onPlantClick
            )
        }
    }
}


@Composable
fun PlantCard(plant: Plant, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clickable { onClick() } // Evento onClick para navegar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically // Centra verticalmente los elementos
        ) {
            // Imagen circular de la planta
            val context = LocalContext.current
            val imageView = remember {
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP // Ajustar imagen al círculo
                }
            }

            // Usar Picasso para cargar la imagen
            if (!plant.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(plant.imageUrl)
                    .error(R.drawable.flora) // Imagen de fallback
                    .into(imageView)
            } else {
                Picasso.get()
                    .load(R.drawable.flora) // Imagen predeterminada
                    .into(imageView)
            }

            AndroidView(
                { imageView },
                modifier = Modifier
                    .size(80.dp) // Tamaño del círculo
                    .clip(CircleShape) // Forma circular
                    .border(1.dp, Color.Black , CircleShape) // Borde opcional
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Información textual de la planta
            Column(
                modifier = Modifier.weight(1f) // Ocupa el espacio disponible
            ) {
                Text(
                    text = plant.name,
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = plant.scientificName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Ícono de flecha
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right), // Reemplaza con tu ícono de flecha
                contentDescription = "Ver más detalles",
                tint = Color(0xFF247E3D),
                modifier = Modifier
                    .size(24.dp) // Tamaño del ícono
                    .clickable { onClick() } // Acciona el mismo evento que el resto de la tarjeta
            )
        }
    }
}




@Composable
fun PlantList(plantList: List<Plant>, onPlantClick: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn {
        items(plantList) { plant ->
            PlantCard(plant = plant, onClick = { onPlantClick(plant.name) })  // Pasa el nombre de la planta
        }
    }
}

@Composable
fun Preview(plant: Plant, modifier: Modifier = Modifier, navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Imagen de la planta con diseño redondeado
        val imageView = remember { ImageView(context) }
        Picasso.get()
            .load(plant.imageUrl)
            .error(R.drawable.flora)
            .into(imageView)

        AndroidView(
            { imageView },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp)))


        Spacer(modifier = Modifier.height(16.dp))

        // Nombre de la planta
        Text(
            text = plant.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF247E3D),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Divider()

        // Descripción de la planta
        Text(
            text = plant.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Hipervínculo
        if (plant.source.isNotEmpty()) {
            Text(
                text = "Ver más información:",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ClickableText(
                text = androidx.compose.ui.text.AnnotatedString("Haz clic aquí"),
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(plant.source))
                    context.startActivity(intent)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()

        // Botón para regresar
        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF247E3D),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Regresar a la lista de plantas",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}



