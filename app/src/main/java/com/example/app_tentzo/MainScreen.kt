package com.example.app_tentzo

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.app_tentzo.pages.IdentificarPlanta
import com.example.app_tentzo.pages.MapaScreen
import com.example.app_tentzo.pages.Biblioteca
import com.example.app_tentzo.pages.MapaScreen
import com.example.app_tentzo.pages.PerfilPage



@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navItemList = listOf(
        NavItem("Biblioteca", iconPainter = painterResource(id = R.drawable.baseline_book_24)),
        NavItem("Mapa", iconPainter = painterResource(id = R.drawable.baseline_map_24)),
        NavItem("Identificar Flora", iconPainter = painterResource(id = R.drawable.baseline_camera_alt_24)),
        NavItem("Perfil", iconVector = Icons.Default.Person)
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFE9F4CA)) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            if (navItem.iconVector != null) {
                                androidx.compose.material3.Icon(
                                    imageVector = navItem.iconVector,
                                    contentDescription = navItem.label,
                                    tint = if (selectedIndex == index) Color(0xFF247E3C) else Color.Gray
                                )
                            } else if (navItem.iconPainter != null) {
                                androidx.compose.material3.Icon(
                                    painter = navItem.iconPainter,
                                    contentDescription = navItem.label,
                                    tint = if (selectedIndex == index) Color(0xFF247E3C) else Color.Gray
                                )
                            }
                        },
                        label = { Text(text = navItem.label, textAlign = TextAlign.Center) }
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(modifier = Modifier.padding(innerPadding), selectedIndex)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int) {
    when (selectedIndex) {
        0 -> Biblioteca() // Página Biblioteca
        1 -> MapaScreen()       // Página Mapa
        2 -> IdentificarPlanta() // Página Identificar Flora
        3 -> PerfilPage()     // Página Perfil (incluye LogOut)
    }
}
