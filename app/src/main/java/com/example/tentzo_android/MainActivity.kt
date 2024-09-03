package com.example.tentzo_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tentzo_android.ui.theme.TentzoandroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TentzoandroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    IniciarSesion(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun IniciarSesion(modifier: Modifier = Modifier) {
    val emailState = remember { mutableStateOf("") }
    val contrasena = remember { mutableStateOf("") }
    Column(

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
    ) {

        Text(
            text = "Iniciar Sesión",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.padding(16.dp))
        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text("Email") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            value = contrasena.value,
            onValueChange = { contrasena.value = it },
            label = { Text("Contraseña") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.padding(70.dp))
        Button(
            onClick = {},

            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF247E3C)),
            modifier = Modifier
                .fillMaxWidth(0.8f)
        ) {
            Text(text = "Iniciar Sesión")
        }


        Button(
            onClick = {},

            colors = ButtonDefaults.buttonColors(containerColor = Color(0)),
            modifier = Modifier
                .fillMaxWidth(0.8f)
        ) {
            Text(
                text = "Olvide mi contraseña",
                color = Color(0xFF247E3C)
            )

        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TentzoandroidTheme {
        IniciarSesion()
    }
}