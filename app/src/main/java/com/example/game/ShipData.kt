package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple

data class ShipInfo(
    val id: String,
    val name: String,
    val description: String,
    val priceStars: Int,
    val primaryColor: Color,
    val accentColor: Color,
    val speedMultiplier: Float,
    val initialShields: Int,
    val magnetRadius: Float,
    val iconName: String
)

object ShipCatalog {
    val SHIPS = listOf(
        ShipInfo(
            id = "scout",
            name = "Éclaireur Stellaire",
            description = "Vaisseau polyvalent équilibré, idéal pour manœuvrer entre les astéroïdes.",
            priceStars = 0,
            primaryColor = NeonCyan,
            accentColor = NeonGold,
            speedMultiplier = 1.0f,
            initialShields = 1,
            magnetRadius = 0.12f,
            iconName = "scout"
        ),
        ShipInfo(
            id = "falcon",
            name = "Faucon Néon",
            description = "Ailes fuselées et vitesse de réaction accrue pour des esquives millimétrées.",
            priceStars = 40,
            primaryColor = NeonGold,
            accentColor = NeonCyan,
            speedMultiplier = 1.25f,
            initialShields = 1,
            magnetRadius = 0.16f,
            iconName = "falcon"
        ),
        ShipInfo(
            id = "titan",
            name = "Titan Cybernétique",
            description = "Blindage spatial lourd. Commence chaque partie avec 2 charges de bouclier !",
            priceStars = 100,
            primaryColor = NeonPurple,
            accentColor = NeonCyan,
            speedMultiplier = 0.95f,
            initialShields = 2,
            magnetRadius = 0.15f,
            iconName = "titan"
        ),
        ShipInfo(
            id = "viper",
            name = "Vipère Plasma",
            description = "Furoncle stellaire équipé d'un champ magnétique géant pour aspirer toutes les étoiles.",
            priceStars = 200,
            primaryColor = NeonGreen,
            accentColor = NeonGold,
            speedMultiplier = 1.2f,
            initialShields = 2,
            magnetRadius = 0.24f,
            iconName = "viper"
        )
    )

    fun getShip(id: String): ShipInfo {
        return SHIPS.find { it.id == id } ?: SHIPS.first()
    }
}
