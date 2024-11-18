package com.example.app_tentzo.pages

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.app_tentzo.MainActivity
import com.example.app_tentzo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

@Composable
fun PerfilPage() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val uid = auth.currentUser?.uid ?: return
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showAchievementsMessage by remember { mutableStateOf(false) } // Estado para el mensaje de logros

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { selectedUri ->
                val storageRef = storage.reference.child("profileImages/$uid.jpg")
                storageRef.putFile(selectedUri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            profileImageUrl = downloadUrl.toString()
                            db.collection("users").document(uid)
                                .update("profileImageUrl", profileImageUrl)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Imagen actualizada correctamente", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al guardar la URL en Firestore", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al subir la imagen a Firebase Storage", Toast.LENGTH_SHORT).show()
                    }
            } ?: run {
                Toast.makeText(context, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                name = document.getString("name") ?: "Usuario"
                profileImageUrl = document.getString("profileImageUrl") ?: ""
            } else {
                db.collection("users").document(uid).set(
                    mapOf("name" to "Usuario", "profileImageUrl" to "")
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF247E3D))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .background(Color.White)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // Texto presionable para "Editar Nombre"
                Text(
                    text = "Editar Nombre",
                    color = Color.White, // Texto en blanco
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { isEditing = true } // Acción al presionar
                )
                Spacer(modifier = Modifier.weight(1f))
                // Botón para "Cerrar Sesión"
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = "Cerrar Sesión", color = Color.Red, fontSize = 16.sp)
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(5.dp, Color.Black, CircleShape)
                    .clickable {
                        pickImageLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { context ->
                        android.widget.ImageView(context).apply {
                            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    modifier = Modifier.size(200.dp),
                    update = { imageView ->
                        if (profileImageUrl.isNotEmpty()) {
                            Picasso.get()
                                .load(profileImageUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(imageView)
                        } else {
                            Picasso.get()
                                .load(R.drawable.default_profile)
                                .into(imageView)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        db.collection("users").document(uid).update("name", name)
                            .addOnSuccessListener {
                                isEditing = false
                                Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al actualizar el nombre", Toast.LENGTH_SHORT).show()
                            }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF247E3D))
                ) {
                    Text("Guardar", color = Color.White)
                }
            } else {
                Text(
                    text = name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para mostrar el mensaje de logros
            Button(
                onClick = { showAchievementsMessage = true },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF247E3D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Logros",
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar mensaje si se activa
            if (showAchievementsMessage) {
                Text(
                    text = "Logros no encontrados",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}





