package com.finolize.app.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    fun getIconByName(name: String): ImageVector {
        return when (name) {
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Restaurant" -> Icons.Default.Restaurant
            "Bus" -> Icons.Default.DirectionsBus
            "Movie" -> Icons.Default.Movie
            "Favorite" -> Icons.Default.Favorite
            "Home" -> Icons.Default.Home
            "Work" -> Icons.Default.Work
            "School" -> Icons.Default.School
            "Gym" -> Icons.Default.FitnessCenter
            "Medical" -> Icons.Default.MedicalServices
            "Gas" -> Icons.Default.LocalGasStation
            "Bills" -> Icons.Default.Receipt
            "Travel" -> Icons.Default.Flight
            "Coffee" -> Icons.Default.Coffee
            "Gift" -> Icons.Default.CardGiftcard
            "Pets" -> Icons.Default.Pets
            else -> Icons.Default.Category
        }
    }
}