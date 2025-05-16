package com.example.subclasse_fromzero

import androidx.compose.ui.geometry.Size
import com.example.subclasse_fromzero.logic.Hitbox
import com.example.subclasse_fromzero.logic.enemyHitbox
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import com.example.subclasse_fromzero.logic.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DrawHitboxes(hitboxes: List<Hitbox>, density: Density) {
    hitboxes.forEach { hitbox ->
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        with(density) { hitbox.position.x.toDp() }.roundToPx(),
                        with(density) { hitbox.position.y.toDp() }.roundToPx()
                    )
                }
                .size(
                    width = with(density) { hitbox.size.width.toDp() },
                    height = with(density) { hitbox.size.height.toDp() }
                )
                .background(hitbox.color)
        )
    }
}

@Composable
fun DrawBlockedHitboxes(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    Box(modifier = modifier) {
        blockedHitboxes.forEach { hitbox ->
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            with(density) { hitbox.position.x.toDp().roundToPx() },
                            with(density) { hitbox.position.y.toDp().roundToPx() }
                        )
                    }
                    .size(
                        width = with(density) { hitbox.size.width.toDp() },
                        height = with(density) { hitbox.size.height.toDp() }
                    )
                    .background(hitbox.color)
            )
        }
    }
}


@Composable
fun MapScreen(
    gameViewModel: GameViewModel,
    rpgCharacter: RPGCharacter = remember { RPGCharacter(name = "Jogador", level = 1, position = Offset(640f, 360f)) },
    mapOriginalWidthPx: Float = 2667f,
    mapOriginalHeightPx: Float = 1500f,
    zoomFactor: Float = 1.789f
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val characterWidthPx = with(density) { 48.dp.toPx() }
    val characterHeightPx = with(density) { 48.dp.toPx() }

    val enemies = remember {
        listOf(
            Enemy.create(level = 1, respawnPosition = Offset(1000f, 1000f)),
            Enemy.create(level = 2, respawnPosition = Offset(800f, 650f)),
            Enemy.create(level = 3, respawnPosition = Offset(1800f, 750f))
        )
    }

    val initialPosition = remember { rpgCharacter.reactivePosition }

    var currentHealth by remember { mutableStateOf(rpgCharacter.maxHealth) }
    var lastMovementDelta by remember { mutableStateOf(Offset.Zero) }
    var isMoving by remember { mutableStateOf(false) }
    var showInventory by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var inCombatWith by remember { mutableStateOf<Enemy?>(null) }
    var projectiles by remember { mutableStateOf(listOf<Projectile>()) }
    var damagePopups by remember { mutableStateOf(listOf<Pair<Offset, DamagePopup>>()) }

    // 1. Carregamento das hitboxes do mapa
    var mapHitboxes by remember { mutableStateOf<MapHitboxes?>(null) }
    LaunchedEffect(Unit) {
        mapHitboxes = loadMapHitboxesFromAssets(context, "hitbox_mapa_1.json")
    }
    val hitboxesRaw = mapHitboxes?.hitboxes ?: emptyList()

    // 2. Conversão das hitboxes do JSON para Hitbox da lógica
    val hitboxList = remember(hitboxesRaw) {
        hitboxesRaw.map { h ->
            Hitbox(
                position = Offset(h.x.toFloat(), h.y.toFloat()),
                size = Size(h.width.toFloat(), h.height.toFloat())
            )
        }
    }

    fun limitCharacterPosition(pos: Offset): Offset {
        val offsetLeft = with(density) { -700.dp.toPx() }
        val offsetRight = with(density) { -60.dp.toPx() }
        val offsetTop = with(density) { -230.dp.toPx() }
        val offsetBottom = with(density) { -180.dp.toPx() }

        val minX = offsetLeft
        val maxX = mapOriginalWidthPx - offsetRight
        val minY = offsetTop
        val maxY = mapOriginalHeightPx - offsetBottom

        val x = pos.x.coerceIn(minX, maxX)
        val y = pos.y.coerceIn(minY, maxY)
        return Offset(x, y)
    }

    // 3. Colisão usando hitboxList já convertido
    fun isCollidingWithAll(pos: Offset, size: Size): Boolean {
        val rect = Rect(pos, size)
        val collidesWithEnemies = isColliding(pos, size, enemies)
        val collidesWithHitboxes = hitboxList.any { hitbox ->
            val hitboxRect = Rect(hitbox.position, hitbox.size)
            rect.overlaps(hitboxRect)
        }
        //
        return collidesWithEnemies || collidesWithHitboxes
    }

    // 4. Movimentação usando canto superior esquerdo e colisão
    fun moveCharacter(delta: Offset) {
        val currentPos = rpgCharacter.reactivePosition
        val tentativePos = currentPos + delta

        val characterTopLeft = tentativePos
        val characterHitboxSize = Size(characterWidthPx, characterHeightPx)

        if (!isCollidingWithAll(characterTopLeft, characterHitboxSize)) {
            val limitedPos = limitCharacterPosition(tentativePos)
            rpgCharacter.reactivePosition = limitedPos
        }
    }

    fun onPlayerHit(damage: Int, isCrit: Boolean) {
        currentHealth = (currentHealth - damage).coerceAtLeast(0)
        if (currentHealth == 0) {
            rpgCharacter.reactivePosition = initialPosition
            currentHealth = rpgCharacter.maxHealth
            // Aqui você pode adicionar efeitos visuais, sons, tela de morte, etc.
        }
    }

    fun onEnemyHit(damage: Int, isCrit: Boolean) {
        inCombatWith?.let { enemy ->
            enemy.takeDamage(damage) { droppedItem ->
                // Lógica para drops, se necessário
            }
            if (!enemy.isAlive) {
                inCombatWith = null
            }
        }
    }

    LaunchedEffect(rpgCharacter.reactivePosition, inCombatWith) {
        if (inCombatWith == null) {
            val enemyToFight = enemies.firstOrNull { enemy ->
                enemy.isAlive && distanceBetween(rpgCharacter.reactivePosition, enemy.position) < 100f
            }
            if (enemyToFight != null) {
                inCombatWith = enemyToFight
                scope.launch {
                    startCombatLoop(
                        enemy = enemyToFight,
                        getPlayerPosition = { rpgCharacter.reactivePosition },
                        playerAttack = rpgCharacter.attack,
                        playerDefense = rpgCharacter.defense,
                        playerAttackSpeedMs = (1000 / rpgCharacter.attacksPerSecond).toLong(),
                        onPlayerHit = ::onPlayerHit,
                        onEnemyHit = ::onEnemyHit,
                        addProjectile = { projectile -> projectiles = projectiles + projectile },
                        removeProjectile = { id -> projectiles = projectiles.filterNot { it.id == id } },
                        showDamagePopup = { pos, popup -> damagePopups = damagePopups + (pos to popup) },
                        removeDamagePopup = { id -> damagePopups = damagePopups.filterNot { it.second.id == id } }
                    )
                }
            }
        } else if (inCombatWith?.isAlive == false) {
            inCombatWith = null
        }
    }

    val viewportCenter = Offset(screenWidthPx / 2f, screenHeightPx / 2f)
    val mapOffset = viewportCenter - rpgCharacter.reactivePosition

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isMoving = true },
                    onDragEnd = { isMoving = false },
                    onDragCancel = { isMoving = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val adjustedDelta = dragAmount * 0.285f
                        moveCharacter(adjustedDelta)
                        lastMovementDelta = dragAmount
                    }
                )
            }
    )
    {
        Box(
            modifier = Modifier
                .offset { IntOffset(mapOffset.x.roundToInt(), mapOffset.y.roundToInt()) }
                .scale(zoomFactor)
                .requiredSize(
                    width = with(density) { mapOriginalWidthPx.toDp() },
                    height = with(density) { mapOriginalHeightPx.toDp() }
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.map_1),
                contentDescription = "Mapa",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            DrawHitboxes(hitboxList, density)
            DrawBlockedHitboxes(modifier = Modifier.fillMaxSize())


            enemies.forEach { enemy ->
                enemy.Draw()
                enemy.MoveRandomly { newPos ->
                    true // Ajuste conforme sua lógica de colisão
                }
            }
        }

        projectiles.forEach { projectile ->
            ProjectileView(projectile) { id ->
                projectiles = projectiles.filterNot { it.id == id }
            }
        }

        damagePopups.forEach { (pos, popup) ->
            DamagePopupView(popup, pos) { id ->
                damagePopups = damagePopups.filterNot { it.second.id == id }
            }
        }

        CharacterAnimation(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.Center),
            isMoving = isMoving,
            movementDelta = lastMovementDelta
        )

        Image(
            painter = painterResource(id = R.drawable.inventory_icon),
            contentDescription = "Ícone do Inventário",
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-5).dp, y = 25.dp)
                .clickable { showInventory = true }
        )

        if (showInventory) {
            InventoryScreen(
                gameViewModel = gameViewModel,
                character = rpgCharacter
            )
        }
    }

}
