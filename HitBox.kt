@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.subclasse_fromzero

import android.content.Context
import android.graphics.RectF
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class MapHitboxes(
    val width: Float,
    val height: Float,
    val hitboxes: List<HitboxRect>
)

@Serializable
data class HitboxRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
) {
    // Função para verificar colisão com RectF (Android graphics)
    fun intersects(other: RectF): Boolean {
        val rect = RectF(x, y, x + width, y + height)
        return RectF.intersects(rect, other)
    }
}

// Função para carregar as hitboxes do arquivo JSON dentro da pasta assets
fun loadMapHitboxesFromAssets(context: Context, filename: String): MapHitboxes? {
    return try {
        val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
        Json { ignoreUnknownKeys = true }.decodeFromString<MapHitboxes>(jsonString)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

