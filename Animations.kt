package com.example.subclasse_fromzero

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import com.example.subclasse_fromzero.logic.Hitbox


@Composable
fun CharacterAnimation(
    modifier: Modifier = Modifier,
    isMoving: Boolean,
    movementDelta: Offset
) {
    // Estado para armazenar a direção atual do personagem
    var currentDirection by remember { mutableStateOf(Direction.DOWN) }

    // Atualiza a direção conforme o delta da movimentação
    LaunchedEffect(movementDelta) {
        currentDirection = getDirectionFromDelta(movementDelta)
    }

    // Controle do frame da animação para o estado "andando"
    var animationFrame by remember { mutableStateOf(0) }

    LaunchedEffect(isMoving) {
        if (isMoving) {
            while (true) {
                animationFrame = (animationFrame + 1) % 2 // alterna entre 0 e 1
                delay(300) // tempo entre frames da animação (ajustável)
            }
        } else {
            animationFrame = 0
        }
    }

    // Seleciona o nome do recurso drawable conforme estado e direção
    val spriteName = when (currentDirection) {
        Direction.UP -> if (isMoving) "walk_up_${animationFrame + 1}" else "idle_up"
        Direction.DOWN -> if (isMoving) "walk_down_${animationFrame + 1}" else "idle_down"
        Direction.LEFT -> if (isMoving) "walk_left_${animationFrame + 1}" else "idle_left"
        Direction.RIGHT -> if (isMoving) "walk_right_${animationFrame + 1}" else "idle_right"
    }

    // Obtém o id do recurso drawable dinamicamente
    val context = androidx.compose.ui.platform.LocalContext.current
    val spriteResId = remember(spriteName) {
        context.resources.getIdentifier(spriteName, "drawable", context.packageName)
    }

    Image(
        painter = painterResource(id = spriteResId),
        contentDescription = "Personagem animado",
        modifier = modifier.size(48.dp)
            .simpleShadow(
            color = Color(0x9C000000),
            offsetX = 0.dp,
            offsetY = 7.dp,
            widthRatio = 0.68f,
            heightRatio = 0.3f
        )

    )
}

/**
 * Função que determina a direção principal a partir do delta da movimentação.
 * Para diagonais, escolhe a direção vertical se o componente Y for maior,
 * caso contrário, escolhe horizontal.
 */
fun getDirectionFromDelta(delta: Offset): Direction {
    val threshold = 0.1f // para evitar ruídos pequenos

    if (delta.getDistance() < threshold) {
        // Movimento muito pequeno, manter direção atual (ou DOWN como padrão)
        return Direction.DOWN
    }

    return if (kotlin.math.abs(delta.y) > kotlin.math.abs(delta.x)) {
        if (delta.y < 0) Direction.UP else Direction.DOWN
    } else {
        if (delta.x < 0) Direction.LEFT else Direction.RIGHT
    }
}

fun Modifier.simpleShadow(
    color: Color = Color(0xA8000000),
    offsetX: Dp = 0.dp,
    offsetY: Dp = 7.dp,
    widthRatio: Float = 0.68f,
    heightRatio: Float = 0.3f
) = this.then(
    Modifier.drawBehind {
        val shadowWidth = size.width * widthRatio
        val shadowHeight = size.height * heightRatio
        val centerX = size.width / 2 + offsetX.toPx()
        val centerY = size.height - offsetY.toPx()

        drawOval(
            color = color,
            topLeft = Offset(centerX - shadowWidth / 2, centerY - shadowHeight / 2),
            size = androidx.compose.ui.geometry.Size(shadowWidth, shadowHeight)
        )
    }
)

