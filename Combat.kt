package com.example.subclasse_fromzero.logic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.subclasse_fromzero.Enemy
import com.example.subclasse_fromzero.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

// Dados para popup de dano
data class DamagePopup(
    val damageAmount: Int,
    val critical: Boolean,
    val id: Int = Random.nextInt()
)

// Dados para projétil
data class Projectile(
    val fromPosition: Offset,
    val toPosition: Offset,
    val durationMs: Long = 300L,
    val color: Color,
    val onHit: () -> Unit,
    val id: Int = Random.nextInt()
) {
    var progress by mutableStateOf(0f)

    @Composable
    fun Animate(onAnimationEnd: () -> Unit) {
        LaunchedEffect(Unit) {
            val steps = 30
            val delayPerStep = durationMs / steps
            for (i in 1..steps) {
                progress = i / steps.toFloat()
                delay(delayPerStep)
            }
            onHit()
            onAnimationEnd()
        }
    }

    fun currentPosition(): Offset {
        val dx = toPosition.x - fromPosition.x
        val dy = toPosition.y - fromPosition.y
        return Offset(fromPosition.x + dx * progress, fromPosition.y + dy * progress)
    }
}

@Composable
fun ProjectileView(projectile: Projectile, onRemove: (Int) -> Unit) {
    projectile.Animate {
        onRemove(projectile.id)
    }
    val pos = projectile.currentPosition()
    val projectileResId = R.drawable.projectile_sword

    Image(
        painter = painterResource(id = projectileResId),
        contentDescription = "Projétil de ataque",
        modifier = Modifier.offset {
            IntOffset(pos.x.roundToInt(), pos.y.roundToInt())
        }
    )
}

@Composable
fun DamagePopupView(popup: DamagePopup, position: Offset, onDismiss: (Int) -> Unit) {
    var visible by remember { mutableStateOf(true) }
    if (visible) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BasicText(
                text = popup.damageAmount.toString(),
                modifier = Modifier.offset {
                    IntOffset(position.x.roundToInt(), (position.y - 40).roundToInt())
                },
                style = TextStyle(
                    color = if (popup.critical) Color(0xFFD7D726) else Color(0xA4FF0000),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        LaunchedEffect(Unit) {
            delay(800L)
            visible = false
            onDismiss(popup.id)
        }
    }
}

// Calcula dano base considerando ataque e defesa
fun calculateDamage(attackerAttack: Int, targetDefense: Int): Int {
    return (attackerAttack - targetDefense / 2).coerceAtLeast(1)
}

// Calcula distância Euclidiana entre dois pontos
fun distanceBetween(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}

// Tenta iniciar combate baseado na distância relativa, permitindo múltiplos combates simultâneos
fun CoroutineScope.tryStartCombats(
    playerPosition: Offset,
    enemies: List<Enemy>,
    onCombatStart: (Enemy) -> Unit,
    combatRadius: Float = 60f
) {
    val enemiesToFight = enemies.filter { enemy ->
        enemy.isAlive && distanceBetween(playerPosition, enemy.position) < combatRadius
    }
    enemiesToFight.forEach { enemy ->
        onCombatStart(enemy)
    }
}

// Loop principal do combate
fun CoroutineScope.startCombatLoop(
    enemy: Enemy,
    getPlayerPosition: () -> Offset,
    playerAttack: Int,
    playerDefense: Int,
    playerAttackSpeedMs: Long,
    enemyAttackSpeedMs: Long = 1200L,
    onPlayerHit: (damage: Int, isCrit: Boolean) -> Unit,
    onEnemyHit: (damage: Int, isCrit: Boolean) -> Unit,
    addProjectile: (Projectile) -> Unit,
    removeProjectile: (Int) -> Unit,
    showDamagePopup: (Offset, DamagePopup) -> Unit,
    removeDamagePopup: (Int) -> Unit
) = launch {
    while (enemy.isAlive) {
        val playerPos = getPlayerPosition()
        val enemyPos = enemy.position
        val distancePlayerToEnemy = distanceBetween(playerPos, enemyPos)

        if (distancePlayerToEnemy <= 50f) {
            val damage = calculateDamage(playerAttack, enemy.defense)
            val isCrit = Random.nextFloat() < 0.2f

            val projectile = Projectile(
                fromPosition = playerPos,
                toPosition = enemyPos,
                color = Color(0x66FF0000),
                onHit = {
                    val finalDamage = if (isCrit) (damage * 1.5).toInt() else damage
                    enemy.takeDamage(finalDamage) { droppedItem ->
                        // Lógica para drops, se quiser
                    }
                    onEnemyHit(finalDamage, isCrit)
                    showDamagePopup(enemyPos, DamagePopup(finalDamage, isCrit))
                }
            )
            addProjectile(projectile)
        }

        delay(playerAttackSpeedMs)

        if (!enemy.isAlive) break

        val distanceEnemyToPlayer = distanceBetween(enemyPos, playerPos)

        if (distanceEnemyToPlayer <= 50f) {
            val damage = calculateDamage(enemy.attack, playerDefense)
            val isCrit = Random.nextFloat() < 0.2f
            val finalDamage = if (isCrit) (damage * 1.5).toInt() else damage

            // Aqui o inimigo causa dano ao personagem
            onPlayerHit(finalDamage, isCrit)
            showDamagePopup(playerPos, DamagePopup(finalDamage, isCrit))
        }

        delay(enemyAttackSpeedMs)
    }
}
