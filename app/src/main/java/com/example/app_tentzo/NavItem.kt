package com.example.app_tentzo

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem (
    val label: String,
    val iconVector: ImageVector? = null, // Para íconos de la librería
    val iconPainter: Painter? = null     // Para imágenes personalizadas
)