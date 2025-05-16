package com.example.subclasse_fromzero.logic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.subclasse_fromzero.Enemy

data class Hitbox(
    val position: Offset,
    val size: Size,
    val color: Color = Color(0xA1B73B3B) // cor padrão vermelha semi-transparente
) {
    fun intersects(other: Hitbox): Boolean {
        return !(position.x + size.width <= other.position.x ||
                position.x >= other.position.x + other.size.width ||
                position.y + size.height <= other.position.y ||
                position.y >= other.position.y + other.size.height)
    }
}

// Gera a hitbox do inimigo (tamanho aproximado do sprite)
fun enemyHitbox(enemy: Enemy): Hitbox {
    val size = Size(48f, 48f) // Ajuste conforme o tamanho real do inimigo
    val pos = Offset(enemy.position.x - size.width / 2, enemy.position.y - size.height / 2)
    return Hitbox(pos, size)
}

val blockedHitboxes = listOf(
    Hitbox(Offset(1000f, 500f), Size(200f, 150f), Color(0x4FFF0000)),
//    Hitbox(Offset(1500f, 900f), Size(300f, 100f), Color(0x4FFF0000)),
//    Hitbox(Offset(8f, -180f), Size(2660f, 184f), Color(0x4FFF0000)),
//    Hitbox(Offset(2644f, 4f), Size(104f, 1516f), Color(0x4FFF0000)),
//    Hitbox(Offset(-12f, 1504f), Size(2648f, 24f), Color(0x4FFF0000)),
//    Hitbox(Offset(-27.2727f, -9.09091f), Size(27.2727f, 1542.42f), Color(0x4FFF0000)),
//    Hitbox(Offset(301f, 44f), Size(118f, 115f), Color(0x4FFF0000)),
//    Hitbox(Offset(298f, 5f), Size(228f, 39f), Color(0x4FFF0000)),
//    Hitbox(Offset(-57.5758f, -27.2727f), Size(357.576f, 966.667f), Color(0x4FFF0000)),
//    Hitbox(Offset(-106.061f, 793.939f), Size(521.212f, 239.394f), Color(0x4FFF0000)),
//    Hitbox(Offset(-127.273f, 1030.3f), Size(533.333f, 496.97f), Color(0x4FFF0000)),
//    Hitbox(Offset(382.667f, 1330.67f), Size(126f, 112f), Color(0x4FFF0000)),
//    Hitbox(Offset(387.333f, 1000f), Size(232f, 328.667f), Color(0x4FFF0000)),
//    Hitbox(Offset(342f, 724f), Size(152f, 312f), Color(0x4FFF0000)),
//    Hitbox(Offset(432f, 910f), Size(184f, 110f), Color(0x4FFF0000)),
//    Hitbox(Offset(382f, 6f), Size(180f, 84f), Color(0x4FFF0000)),
//    Hitbox(Offset(280f, 44f), Size(178f, 134f), Color(0x4FFF0000)),
//    Hitbox(Offset(276f, 86f), Size(112f, 712f), Color(0x4FFF0000)),
//    Hitbox(Offset(1366f, 492f), Size(112f, 516f), Color(0x4FFF0000)),
//    Hitbox(Offset(1368f, 480f), Size(320f, 90f), Color(0x4FFF0000)),
//    Hitbox(Offset(1276f, 8f), Size(106f, 82f), Color(0x4FFF0000)),
//    Hitbox(Offset(1280f, 42f), Size(200f, 74f), Color(0x4FFF0000)),
//    Hitbox(Offset(1402f, 68f), Size(84f, 144f), Color(0x4FFF0000)),
//    Hitbox(Offset(1412f, 142f), Size(162f, 90f), Color(0x4FFF0000)),
//    Hitbox(Offset(1506f, 170f), Size(188f, 140f), Color(0x4FFF0000)),
//    Hitbox(Offset(1612f, 240f), Size(78f, 330f), Color(0x4FFF0000)),
//    Hitbox(Offset(1692f, 242f), Size(120f, 114f), Color(0x4FFF0000)),
//    Hitbox(Offset(1806f, 250f), Size(218f, 224f), Color(0x4FFF0000)),
//    Hitbox(Offset(2024f, 252f), Size(608f, 320f), Color(0x4FFF0000)),
//    Hitbox(Offset(2610f, 242f), Size(92f, 332f), Color(0x4FFF0000)),
//    Hitbox(Offset(838f, 152f), Size(104f, 100.667f), Color(0x4FFF0000)),
//    Hitbox(Offset(1170f, 40f), Size(98f, 98f), Color(0x4FFF0000)),
//    Hitbox(Offset(1495f, 370f), Size(98f, 100f), Color(0x4FFF0000)),
//    Hitbox(Offset(402.667f, 245.333f), Size(122.667f, 121.333f), Color(0x4FFF0000))
    //Mais hitboxes aqui se quiser
)


// Função para verificar colisão do personagem com bloqueios fixos, JSON e inimigos
fun isColliding(
    pos: Offset,
    size: Size,
    enemies: List<Enemy>,
    jsonBlockedHitboxes: List<Hitbox> = emptyList()
): Boolean {
    val characterHitbox = Hitbox(pos, size)

    if (blockedHitboxes.any { it.intersects(characterHitbox) }) return true
    if (jsonBlockedHitboxes.any { it.intersects(characterHitbox) }) return true
    if (enemies.any { it.isAlive && enemyHitbox(it).intersects(characterHitbox) }) return true

    return false
}

// Tenta mover o personagem considerando colisão com bloqueios fixos, JSON e inimigos
fun tryMove(
    currentPosition: Offset,
    delta: Offset,
    characterHitboxSize: Size,
    enemies: List<Enemy>,
    jsonBlockedHitboxes: List<Hitbox> = emptyList()
): Offset {
    val newPosition = currentPosition + delta
    val newHitbox = Hitbox(newPosition, characterHitboxSize)

    val collidesWithBlocked = blockedHitboxes.any { it.intersects(newHitbox) } ||
            jsonBlockedHitboxes.any { it.intersects(newHitbox) }
    val collidesWithEnemies = enemies.any { it.isAlive && enemyHitbox(it).intersects(newHitbox) }

    return if (collidesWithBlocked || collidesWithEnemies) currentPosition else newPosition
}
