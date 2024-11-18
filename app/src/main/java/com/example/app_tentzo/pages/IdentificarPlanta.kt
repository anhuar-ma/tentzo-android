package com.example.app_tentzo.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.app_tentzo.ml.PlantModel
import com.google.firebase.database.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.InputStream
import java.util.jar.Manifest



@Composable
fun IdentificarPlanta() {
    val context = LocalContext.current
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultText by remember { mutableStateOf("") }
    var topConfidenceList by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var plants by remember { mutableStateOf(emptyList<Plant>()) }
    var plantInfo by remember { mutableStateOf<Plant?>(null) }
    var cameraPermissionGranted by remember { mutableStateOf(false) }

    // Lanzador para solicitar permisos de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionGranted = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permiso denegado para usar la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // Cargar datos desde Firebase
    LaunchedEffect(Unit) {
        loadPlantsFromDatabase { loadedPlants ->
            plants = loadedPlants
        }
    }

    // Método para clasificar imágenes usando el modelo generado
    fun classifyImage(bitmap: Bitmap): String {
        val model = PlantModel.newInstance(context)

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val inputFeature =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        val byteBuffer = inputFeature.buffer

        val pixels = IntArray(224 * 224)
        resizedBitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)
        for (pixel in pixels) {
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixel and 0xFF) / 255.0f)
        }

        val outputs = model.process(inputFeature)
        val outputFeature = outputs.outputFeature0AsTensorBuffer

        val confidences = outputFeature.floatArray

        val labels = listOf(
            "Sida abutifolia", "Lysiloma acapulcensis", "Acacia acatlensis", "Lantana achyranthifolia",
            "Plumeria acutifolia", "Ceiba aesculifolia", "Conopholis alpina", "Chenopodium ambrosioides",
            "Solanum americanum", "Simsia amplexicaulis", "Pseudosmo dingium andreux", "Agave angustifolia",
            "Malvaviscus arboreus", "Anagallis arvensis", "Bidens aurea",
            "Dalea bicolor", "Acacia bilimekii", "Glandularia bipinnatifida", "Cosmos bipinnatus",
            "Salix bonplandiana", "Psilactis brevilingulata", "Cologania broussoneti",
            "Psittacanthus calyculatus", "Lantana camara", "Conyza canadensis", "Sideroxylon capiri",
            "Ageratum corymbosum", "Ficus cotinifolia",
            "Rumex crispus", "Asclepias curassavica", "Krameria cytisoides", "Lamourouxi a dasyantha",
            "Gomphrena decumbens", "Amelanchier denticulata", "Clematis dioica", "Croton dioicus",
            "Bauhinia dipetala", "Mascagnia dipholiphylla", "Casimiroa edulis", "Echinoptery s eglandulosa",
            "Roldana ehrenbergiana", "Bouvardia erecta", "Leucaena esculenta", "Ageratina espinosarum",
            "Acacia farnesiana", "Harpalyce formosa", "Cercocarpus fothergilloides", "Quercus frutex",
            "Bursera galeottiana", "Echeveria gibbiflora", "Asclepias glaucescens", "Quercus glaucoides",
            "Helianthemu m glomeratum", "Flourensia glutinosa", "Gymnosper ma glutinosum", "Cardiosperm um halicacabum",
            "Senna holwayana", "Karwinskia humboldtiana", "Arachis hypogaea", "Gochnatia hypoleuca",
            "Phytolacca icosandra", "Capparis incana", "Melilotus indicus",
            "Brongniartia intermedia", "Grindelia inuloides", "Limón"
        )





        val confidencePairs = confidences.mapIndexed { index, confidence ->
            labels.getOrElse(index) { "Desconocido" } to "${(confidence * 100).toInt()}%"
        }

        topConfidenceList = confidencePairs.sortedByDescending { it.second.removeSuffix("%").toInt() }.take(3)

        val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
        val resultText = if (maxIndex != -1) {
            "Resultado: ${labels[maxIndex]} (Confianza: ${(confidences[maxIndex] * 100).toInt()}%)"
        } else {
            "No se pudo clasificar la imagen."
        }

        model.close()

        return resultText
    }


    // Lanzadores para abrir cámara y galería
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            selectedBitmap = bitmap
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Seleccionar o Capturar Imagen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF247E3D)),
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraPermissionGranted = true
                            cameraLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Abrir Cámara")
                }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF247E3D)),
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Abrir Galería")
                }
            }
        }

        item {
            selectedBitmap?.let { bitmap ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF247E3D)),
                    onClick = { resultText = classifyImage(bitmap) },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(text = "Clasificar Imagen")
                }
            }
        }

        item {
            Text(
                text = resultText,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(topConfidenceList) { confidence ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = confidence.first, fontWeight = FontWeight.Bold)
                Text(text = confidence.second)
            }
        }

        item {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF247E3D)),
                onClick = {
                    if (topConfidenceList.isNotEmpty()) {
                        val labelWithHighestConfidence = topConfidenceList.first().first

                        plantInfo = plants.firstOrNull { it.scientificName.equals(labelWithHighestConfidence, true) }

                        if (plantInfo == null) {
                            Toast.makeText(context, "No se encontró información para esta planta", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Clasifique la imagen primero", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(text = "Ver Información Disponible")
            }
        }

        // Mostrar información de la planta en LazyColumn
        plantInfo?.let { plant ->
            item {
                Text(
                    text = "Información de la Planta",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(listOf("Nombre científico: ${plant.scientificName}","Nombre común: ${plant.name}" ,
                "Descripción: ${plant.description}")) { detail ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Text(
                        text = detail,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Espacio adicional
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}


// Función para cargar plantas desde Firebase
fun loadPlantsFromDatabase(onPlantsLoaded: (List<Plant>) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("plants")

    database.get().addOnSuccessListener { snapshot ->
        val plantList = snapshot.children.mapNotNull { it.getValue<Plant>() }
        onPlantsLoaded(plantList)
    }

    database.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val plantList = dataSnapshot.children.mapNotNull { it.getValue<Plant>() }
            onPlantsLoaded(plantList)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Manejar errores
        }
    })
}



