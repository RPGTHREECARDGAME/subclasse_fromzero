package com.example.subclasse_fromzero

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.subclasse_fromzero.GameData
import com.example.subclasse_fromzero.logic.tryMove
import com.example.subclasse_fromzero.Enemy
import androidx.compose.ui.geometry.Size
import com.example.subclasse_fromzero.logic.Hitbox

data class RPGCharacter(
    var name: String,
    var level: Int = 1,
    var currentExperience: Int = 0,

    // Posição do personagem no mundo (mapa)
    var position: Offset = Offset.Zero,

    // Velocidade de movimento em pixels por segundo (ajuste conforme necessário)
    var movementSpeed: Float = GameData.BASE_MOVEMENT_SPEED,

    // Dimensões do personagem para hitbox e renderização
    val width: Dp = 48.dp,
    val height: Dp = 48.dp
) {

    // Estado interno reativo para position
    private var _positionState by mutableStateOf(position)

    // Sincroniza a propriedade pública position com o estado interno
    var reactivePosition: Offset
        get() = _positionState
        set(value) {
            _positionState = value
            position = value
        }

    // Status base do personagem
    var maxHealth by mutableStateOf(GameData.BASE_HEALTH)
    var attack by mutableStateOf(GameData.BASE_ATTACK)
    var defense by mutableStateOf(GameData.BASE_DEFENSE)
    var critRate by mutableStateOf(GameData.BASE_CRIT_RATE)
    var critDamage by mutableStateOf(GameData.BASE_CRIT_DAMAGE)
    var attacksPerSecond by mutableStateOf(GameData.BASE_ATTACK_SPEED)
    var lifesteal by mutableStateOf(GameData.BASE_LIFESTEAL)

    // Itens equipados
    private val equippedItems = mutableListOf<Item>()

    init {
        updateStats()
        // Inicializa o estado reativo com a posição inicial
        reactivePosition = position
    }

    fun getEquippedItems(): List<Item> = equippedItems.toList()

    fun updateStats() {
        val progression = GameData.getProgression(level)

        maxHealth = progression.maxHealth
        attack = progression.attack
        defense = progression.defense
        critRate = progression.critRate
        critDamage = progression.critDamage
        attacksPerSecond = progression.attacksPerSecond
        movementSpeed = progression.movementSpeed
        lifesteal = progression.lifesteal

        equippedItems.forEach { item ->
            maxHealth += item.healthBonus
            attack += item.attackBonus
            defense += item.defenseBonus
            critRate += item.criticalChanceBonus
            critDamage += item.criticalDamageBonus
            attacksPerSecond += item.attackSpeedBonus
            movementSpeed += item.movementSpeedBonus
            lifesteal += item.lifestealBonus
        }
    }

    fun equipItem(item: Item) {
        equippedItems.add(item)
        updateStats()
    }

    fun unequipItem(item: Item) {
        equippedItems.remove(item)
        updateStats()
    }

    fun addExperience(experience: Int) {
        currentExperience += experience
        while (currentExperience >= GameData.getProgression(level).maxExperience) {
            currentExperience -= GameData.getProgression(level).maxExperience
            levelUp()
        }
    }

    private fun levelUp() {
        level += 1
        updateStats()
    }

    // --------- Movimentação ---------

    /**
     * Atualiza a posição do personagem, somando o delta.
     * Aqui adiciona verificação de colisão antes de aplicar o movimento.
     */
    fun moveBy(
        delta: Offset,
        density: Density,
        enemies: List<Enemy>,
        mapHitboxes: List<Hitbox> // novo parâmetro para colisão com mapa
    ) {
        val characterSizePx = Size(
            width = with(density) { width.toPx() },
            height = with(density) { height.toPx() }
        )

        val validatedPos = tryMove(
            reactivePosition,
            delta,
            characterSizePx,
            enemies,
            mapHitboxes
        )
        reactivePosition = validatedPos
    }

    /**
     * Retorna a hitbox atual do personagem no mundo.
     * ATENÇÃO: Essa função retorna valores em dp, não convertidos para pixels.
     * Para usar em cálculos de colisão, prefira usar getHitboxPx(density).
     */
    fun getHitbox(): Rect {
        return Rect(
            left = reactivePosition.x,
            top = reactivePosition.y,
            right = reactivePosition.x + width.value,
            bottom = reactivePosition.y + height.value
        )
    }

    /**
     * Retorna a hitbox atual do personagem convertida para pixels,
     * usando o objeto Density passado como parâmetro.
     * Use essa função dentro de Composables ou onde tiver acesso ao Density.
     */
    fun getHitboxPx(density: Density): Rect {
        val widthPx = with(density) { width.toPx() }
        val heightPx = with(density) { height.toPx() }
        return Rect(
            left = reactivePosition.x,
            top = reactivePosition.y,
            right = reactivePosition.x + widthPx,
            bottom = reactivePosition.y + heightPx
        )
    }
}
