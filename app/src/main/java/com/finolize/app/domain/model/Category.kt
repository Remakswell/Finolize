package com.finolize.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Category(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

object CategoryList {
    val categories = listOf(
        Category("Food", Icons.Default.Restaurant, Color(0xFFFF9800)),
        Category("Transport", Icons.Default.DirectionsBus, Color(0xFF2196F3)),
        Category("Shopping", Icons.Default.ShoppingCart, Color(0xFFE91E63)),
        Category("Entertainment", Icons.Default.Movie, Color(0xFF9C27B0)),
        Category("Health", Icons.Default.Favorite, Color(0xFF4CAF50)),
        Category("General", Icons.Default.Payments, Color(0xFF607D8B))
    )

    fun getCategoryByName(name: String): Category {
        return categories.find { it.name == name } ?: categories.last()
    }
}
