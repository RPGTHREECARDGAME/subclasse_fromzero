package com.example.subclasse_fromzero

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

// Grades específicas para inimigos (sem relação com itens)
enum class EnemyGrade {
    NO_GRADE,
    D_GRADE,
    C_GRADE,
    B_GRADE,
    A_GRADE,
    S_GRADE
}

fun getEnemyGradeForLevel(level: Int): EnemyGrade = when {
    level < 10 -> EnemyGrade.NO_GRADE
    level < 25 -> EnemyGrade.D_GRADE
    level < 35 -> EnemyGrade.C_GRADE
    level < 52 -> EnemyGrade.B_GRADE
    level < 70 -> EnemyGrade.A_GRADE
    else       -> EnemyGrade.S_GRADE
}

class Enemy(
    val level: Int,
    val initialHp: Int,
    val attack: Int,
    val defense: Int,
    val expReward: Int,
    val respawnPosition: Offset
) {
    var health by mutableStateOf(initialHp)
    val maxHealth = initialHp
    var position by mutableStateOf(respawnPosition)

    var isAlive by mutableStateOf(true)
    var isMoving by mutableStateOf(false)
    var movementDelta by mutableStateOf(Offset.Zero)
    var isAttacking by mutableStateOf(false)

    private var currentDirection by mutableStateOf(Direction.DOWN)

    private fun updateDirection(delta: Offset) {
        val threshold = 0.1f
        if (delta.getDistance() < threshold) return
        currentDirection = if (abs(delta.y) > abs(delta.x)) {
            if (delta.y < 0) Direction.UP else Direction.DOWN
        } else {
            if (delta.x < 0) Direction.LEFT else Direction.RIGHT
        }
    }

    @Composable
    fun Draw() {
        val context = LocalContext.current
        var animationFrame by remember { mutableStateOf(0) }
        var visible by remember { mutableStateOf(true) }

        LaunchedEffect(movementDelta) {
            updateDirection(movementDelta)
        }

        LaunchedEffect(isMoving) {
            if (isMoving) {
                while (true) {
                    animationFrame = (animationFrame + 1) % 2
                    delay(300)
                }
            } else {
                animationFrame = 0
            }
        }

        LaunchedEffect(isAlive) {
            if (!isAlive) {
                visible = true
                delay(200)
                visible = false
            } else {
                visible = true
            }
        }

        val spriteName = when (currentDirection) {
            Direction.UP -> if (isMoving) "walk_up_${animationFrame + 1}" else "idle_up"
            Direction.DOWN -> if (isMoving) "walk_down_${animationFrame + 1}" else "idle_down"
            Direction.LEFT -> if (isMoving) "walk_left_${animationFrame + 1}" else "idle_left"
            Direction.RIGHT -> if (isMoving) "walk_right_${animationFrame + 1}" else "idle_right"
        }

        val spriteResId = remember(spriteName) {
            context.resources.getIdentifier(spriteName, "drawable", context.packageName)
        }

        AnimatedVisibility(
            visible = visible,
            exit = fadeOut(animationSpec = tween(durationMillis = 200))
        ) {
            Image(
                painter = painterResource(id = spriteResId),
                contentDescription = "Inimigo animado",
                modifier = Modifier
                    .size(48.dp)
                    .drawBehind {
                        val shadowWidth = size.width * 0.68f
                        val shadowHeight = size.height * 0.3f
                        val centerX = size.width / 2
                        val centerY = size.height - 7.dp.toPx()
                        drawOval(
                            color = Color(0x9C000000),
                            topLeft = androidx.compose.ui.geometry.Offset(centerX - shadowWidth / 2, centerY - shadowHeight / 2),
                            size = androidx.compose.ui.geometry.Size(shadowWidth, shadowHeight)
                        )
                    }
                    .offset {
                        IntOffset(position.x.roundToInt(), position.y.roundToInt())
                    }
            )
        }
    }

    /**
     * Movimentação aleatória respeitando limites e colisões.
     * Passe uma função canMoveTo(newPos: Offset): Boolean que retorna true se o movimento é permitido.
     */
    @Composable
    fun MoveRandomly(canMoveTo: (Offset) -> Boolean) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(3000L)
                val direction = (0..3).random()
                val delta = when (direction) {
                    0 -> Offset(0f, -30f)
                    1 -> Offset(0f, 30f)
                    2 -> Offset(-30f, 0f)
                    else -> Offset(30f, 0f)
                }
                val newPos = position + delta
                if (canMoveTo(newPos)) {
                    movementDelta = delta
                    position = newPos
                    isMoving = true
                } else {
                    movementDelta = Offset.Zero
                    isMoving = false
                }
                delay(500)
                isMoving = false
                movementDelta = Offset.Zero
            }
        }
    }

    /**
     * Aplica dano ao inimigo. Retorna true se o inimigo morreu.
     * onDeath é chamado quando o inimigo morre.
     */
    fun takeDamage(amount: Int, onDeath: (Item) -> Unit): Boolean {
        if (!isAlive) return false
        health = (health - amount).coerceAtLeast(0)
        if (health == 0) {
            die(onDeath)
            return true
        }
        return false
    }

    private fun die(onDeath: (Item) -> Unit) {
        isAlive = false
        val droppedItem = dropItem()
        onDeath(droppedItem)
    }

    fun respawn() {
        health = maxHealth
        position = respawnPosition
        isAlive = true
    }

    fun dropItem(): Item {
        return generateDroppedItem(level)
    }

    companion object {
        private data class GradeBaseStats(
            val baseHp: Int,
            val baseAttack: Int,
            val baseDefense: Int,
            val baseExp: Int,
            val hpGrowthPerLevel: Int,
            val attackGrowthPerLevel: Int,
            val defenseGrowthPerLevel: Int,
            val expGrowthPerLevel: Int
        )

        private val gradeStatsMap = mapOf(
            EnemyGrade.NO_GRADE to GradeBaseStats(
                baseHp = 50, baseAttack = 5, baseDefense = 2, baseExp = 10,
                hpGrowthPerLevel = 5, attackGrowthPerLevel = 1, defenseGrowthPerLevel = 1, expGrowthPerLevel = 2
            ),
            EnemyGrade.D_GRADE to GradeBaseStats(
                baseHp = 100, baseAttack = 10, baseDefense = 5, baseExp = 20,
                hpGrowthPerLevel = 8, attackGrowthPerLevel = 2, defenseGrowthPerLevel = 1, expGrowthPerLevel = 4
            ),
            EnemyGrade.C_GRADE to GradeBaseStats(
                baseHp = 150, baseAttack = 15, baseDefense = 8, baseExp = 35,
                hpGrowthPerLevel = 10, attackGrowthPerLevel = 3, defenseGrowthPerLevel = 2, expGrowthPerLevel = 6
            ),
            EnemyGrade.B_GRADE to GradeBaseStats(
                baseHp = 250, baseAttack = 25, baseDefense = 15, baseExp = 60,
                hpGrowthPerLevel = 15, attackGrowthPerLevel = 4, defenseGrowthPerLevel = 3, expGrowthPerLevel = 8
            ),
            EnemyGrade.A_GRADE to GradeBaseStats(
                baseHp = 400, baseAttack = 40, baseDefense = 25, baseExp = 100,
                hpGrowthPerLevel = 20, attackGrowthPerLevel = 5, defenseGrowthPerLevel = 4, expGrowthPerLevel = 12
            ),
            EnemyGrade.S_GRADE to GradeBaseStats(
                baseHp = 700, baseAttack = 70, baseDefense = 40, baseExp = 180,
                hpGrowthPerLevel = 30, attackGrowthPerLevel = 8, defenseGrowthPerLevel = 6, expGrowthPerLevel = 20
            )
        )

        fun create(level: Int, respawnPosition: Offset): Enemy {
            val grade = getEnemyGradeForLevel(level)
            val baseStats = gradeStatsMap[grade] ?: error("Grade stats not found for $grade")

            val gradeMinLevel = when (grade) {
                EnemyGrade.NO_GRADE -> 1
                EnemyGrade.D_GRADE -> 10
                EnemyGrade.C_GRADE -> 25
                EnemyGrade.B_GRADE -> 35
                EnemyGrade.A_GRADE -> 52
                EnemyGrade.S_GRADE -> 70
            }

            val levelOffset = level - gradeMinLevel
            val hp = baseStats.baseHp + baseStats.hpGrowthPerLevel * levelOffset.coerceAtLeast(0)
            val attack = baseStats.baseAttack + baseStats.attackGrowthPerLevel * levelOffset.coerceAtLeast(0)
            val defense = baseStats.baseDefense + baseStats.defenseGrowthPerLevel * levelOffset.coerceAtLeast(0)
            val expReward = baseStats.baseExp + baseStats.expGrowthPerLevel * levelOffset.coerceAtLeast(0)

            return Enemy(
                level = level,
                initialHp = hp,
                attack = attack,
                defense = defense,
                expReward = expReward,
                respawnPosition = respawnPosition
            )
        }
    }
}
