package com.example.subclasse_fromzero
import kotlin.random.Random

// 1. ENUMS
enum class ItemGrade { NO_GRADE, D_GRADE, C_GRADE, B_GRADE, A_GRADE, S_GRADE }
enum class ItemRarity { COMMON, INCOMMON, RARE, MYTHICAL, LEGENDARY }
enum class DataItem { HELMET, CHEST, BOOTS, LEGS, WEAPON, TATTOO }
enum class WeaponType { AXE, DAGGER, SPEAR, CROSSBOW }

// 2. DATA CLASS ITEM
data class Item(
    val id: Int,
    val name: String,
    val iconName: String = "",
    val iconRes: Int = 0,
    val type: DataItem,
    val weaponType: WeaponType? = null, // só usado se type == WEAPON
    val grade: ItemGrade,
    val rarity: ItemRarity,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val healthBonus: Int = 0,
    val healthPercentBonus: Float = 0f,
    val attackSpeedBonus: Float = 0f,
    val criticalChanceBonus: Float = 0f,
    val criticalDamageBonus: Float = 0f,
    val movementSpeedBonus: Float = 0f,
    val lifestealBonus: Float = 0f,
    val description: String = ""
)

// 3. DATA CLASS INVENTORY
data class Inventory(
    val slots: Int,
    val items: List<Item> = emptyList()
)

// 4. TABELAS DE CONFIGURAÇÃO

fun getItemGradeForLevel(level: Int): ItemGrade = when {
    level < 10 -> ItemGrade.NO_GRADE
    level < 25 -> ItemGrade.D_GRADE
    level < 35 -> ItemGrade.C_GRADE
    level < 52 -> ItemGrade.B_GRADE
    level < 70 -> ItemGrade.A_GRADE
    else       -> ItemGrade.S_GRADE
}

val rarityChances: List<Pair<ItemRarity, Float>> = listOf(
    ItemRarity.COMMON to 0.40f,
    ItemRarity.INCOMMON to 0.20f,
    ItemRarity.RARE to 0.10f,
    ItemRarity.MYTHICAL to 0.05f,
    ItemRarity.LEGENDARY to 0.01f,
)

// Ranges de status principal por grade
val gradeStatusRanges: Map<ItemGrade, IntRange> = mapOf(
    ItemGrade.NO_GRADE to (10..20),
    ItemGrade.D_GRADE  to (20..30),
    ItemGrade.C_GRADE  to (30..40),
    ItemGrade.B_GRADE  to (50..60),
    ItemGrade.A_GRADE  to (70..80),
    ItemGrade.S_GRADE  to (90..100),
)

// Ranges de bônus principal por raridade e tipo
val mainBonusRanges: Map<ItemRarity, Map<DataItem, IntRange>> = mapOf(
    ItemRarity.COMMON to mapOf(
        DataItem.HELMET to (10..20),
        DataItem.CHEST to (10..20),
        DataItem.BOOTS to (1..5),
        DataItem.LEGS to (10..20),
        DataItem.WEAPON to (10..20),
        DataItem.TATTOO to (1..5)
    ),
    ItemRarity.INCOMMON to mapOf(
        DataItem.BOOTS to (5..8),
        DataItem.TATTOO to (5..10)
    ),
    ItemRarity.RARE to mapOf(
        DataItem.BOOTS to (8..12)
    ),
    ItemRarity.MYTHICAL to mapOf(
        DataItem.BOOTS to (12..16),
        DataItem.TATTOO to (15..20)
    ),
    ItemRarity.LEGENDARY to mapOf(
        DataItem.BOOTS to (16..20),
        DataItem.TATTOO to (20..25)
    )
)

// Ranges de bônus adicionais por raridade
val bonusRangesByRarity: Map<ItemRarity, Map<String, IntRange>> = mapOf(
    ItemRarity.COMMON to mapOf(
        "lifestealBonus" to (1..5),
        "criticalChanceBonus" to (5..10),
        "movementSpeedBonus" to (1..5),
        "criticalDamageBonus" to (5..10),
        "attackSpeedBonus" to (1..5)
    ),
    ItemRarity.INCOMMON to mapOf(
        "lifestealBonus" to (5..10),
        "criticalChanceBonus" to (10..25),
        "movementSpeedBonus" to (5..8),
        "criticalDamageBonus" to (10..25),
        "attackSpeedBonus" to (5..10)
    ),
    ItemRarity.RARE to mapOf(
        "lifestealBonus" to (10..25),
        "criticalChanceBonus" to (25..40),
        "movementSpeedBonus" to (8..12),
        "criticalDamageBonus" to (25..40),
        "attackSpeedBonus" to (10..25)
    ),
    ItemRarity.MYTHICAL to mapOf(
        "lifestealBonus" to (25..40),
        "criticalChanceBonus" to (40..60),
        "movementSpeedBonus" to (12..16),
        "criticalDamageBonus" to (40..60),
        "attackSpeedBonus" to (25..40)
    ),
    ItemRarity.LEGENDARY to mapOf(
        "lifestealBonus" to (40..60),
        "criticalChanceBonus" to (60..85),
        "movementSpeedBonus" to (16..20),
        "criticalDamageBonus" to (60..85),
        "attackSpeedBonus" to (40..60)
    )
)

// 5. FUNÇÕES DE SORTEIO

fun rollRarity(): ItemRarity {
    val roll = Random.nextFloat()
    var acc = 0f
    for ((rarity, chance) in rarityChances) {
        acc += chance
        if (roll < acc) return rarity
    }
    return ItemRarity.COMMON
}

fun rollBonus(rarity: ItemRarity): List<Pair<String, Int>> {
    val bonusCount = when (rarity) {
        ItemRarity.COMMON    -> if (Random.nextFloat() < 0.25f) 1 else 0
        ItemRarity.INCOMMON  -> if (Random.nextFloat() < 0.25f) 2 else 0
        ItemRarity.RARE      -> if (Random.nextFloat() < 0.25f) 3 else 0
        ItemRarity.MYTHICAL  -> if (Random.nextFloat() < 0.25f) 4 else 0
        ItemRarity.LEGENDARY -> if (Random.nextFloat() < 0.25f) 5 else 0
    }
    val bonuses: MutableList<Pair<String, Int>> = mutableListOf()
    val possibleBonuses = bonusRangesByRarity[rarity]?.toList()?.shuffled() ?: emptyList()
    repeat(bonusCount) {
        if (possibleBonuses.isNotEmpty()) {
            val (attr, range) = possibleBonuses[it % possibleBonuses.size]
            bonuses.add(attr to range.random())
        }
    }
    return bonuses
}

// 6. SPRITE DINÂMICO

fun getIconResByName(iconName: String): Int {
    return try {
        com.example.subclasse_fromzero.R.drawable::class.java.getField(iconName).getInt(null)
    } catch (e: Exception) {
        0 // ou R.drawable.ic_default
    }
}

fun generateSpriteName(type: DataItem, weaponType: WeaponType?, rarity: ItemRarity): String {
    val typeName = when (type) {
        DataItem.WEAPON -> weaponType?.name?.lowercase() ?: "axe"
        DataItem.HELMET -> "helmet"
        DataItem.CHEST -> "chest"
        DataItem.BOOTS -> "boots"
        DataItem.LEGS -> "legs"
        DataItem.TATTOO -> "tattoo"
    }
    // Weapons SEMPRE usam "comum"
    val rarityName = when (type) {
        DataItem.WEAPON -> "comum"
        else -> when (rarity) {
            ItemRarity.COMMON -> "comum"
            ItemRarity.INCOMMON -> "incomum"
            ItemRarity.RARE -> "raro"
            ItemRarity.MYTHICAL -> "mitico"
            ItemRarity.LEGENDARY -> "lendario"
        }
    }
    return "${typeName}_${rarityName}"
}

// 7. DROP DE ITEM

fun generateDroppedItem(level: Int): Item {

    val grade = getItemGradeForLevel(level)
    val rarity = rollRarity()
    val itemTypes = DataItem.values()
    val type = itemTypes[Random.nextInt(itemTypes.size)]

    // Sorteia arma específica se for WEAPON
    val weaponType = if (type == DataItem.WEAPON) {
        WeaponType.values()[Random.nextInt(WeaponType.values().size)]
    } else null

    val mainValue = mainBonusRanges[rarity]?.get(type)?.random()
        ?: gradeStatusRanges[grade]?.random()
        ?: 0

    val iconName = generateSpriteName(type, weaponType, rarity)
    val iconRes = getIconResByName(iconName)

    var item = Item(
        id = Random.nextInt(1000000),
        name = "${rarity.name} ${grade.name} ${weaponType?.name ?: type.name}",
        iconName = iconName,
        iconRes = iconRes,
        type = type,
        weaponType = weaponType,
        grade = grade,
        rarity = rarity,
        description = "${rarity.name} ${grade.name} ${weaponType?.name ?: type.name}"
    )

    // Aplica o bônus principal de acordo com o tipo
    item = when (type) {
        DataItem.HELMET, DataItem.CHEST, DataItem.LEGS -> item.copy(defenseBonus = mainValue)
        DataItem.WEAPON -> item.copy(attackBonus = mainValue)
        DataItem.BOOTS -> item.copy(movementSpeedBonus = mainValue / 100f)
        DataItem.TATTOO -> item.copy(attackSpeedBonus = mainValue / 100f)
    }

    // Define quantos bônus adicionais sortear conforme raridade
    val bonusCount = when (rarity) {
        ItemRarity.COMMON -> 0
        ItemRarity.INCOMMON -> 1
        ItemRarity.RARE -> 2
        ItemRarity.MYTHICAL -> 3
        ItemRarity.LEGENDARY -> 4
    }

    // Define os possíveis bônus conforme tipo
    val possibleBonuses = if (type == DataItem.TATTOO) {
        listOf("attackSpeedBonus")
    } else {
        listOf("lifestealBonus", "criticalChanceBonus", "movementSpeedBonus", "criticalDamageBonus")
    }

    val bonusRanges = bonusRangesByRarity[rarity] ?: emptyMap()

    // Sorteia os bônus adicionais
    var bonuses = mutableListOf<Pair<String, Int>>()
    repeat(bonusCount) {
        val bonusType = possibleBonuses.random()
        val range = bonusRanges[bonusType] ?: (1..1)
        bonuses.add(bonusType to range.random())
    }

    // Aplica os bônus adicionais no item
    for ((attr, value) in bonuses) {
        item = when (attr) {
            "lifestealBonus" -> item.copy(lifestealBonus = item.lifestealBonus + value / 100f)
            "criticalChanceBonus" -> item.copy(criticalChanceBonus = item.criticalChanceBonus + value / 100f)
            "movementSpeedBonus" -> item.copy(movementSpeedBonus = item.movementSpeedBonus + value / 100f)
            "criticalDamageBonus" -> item.copy(criticalDamageBonus = item.criticalDamageBonus + value / 100f)
            "attackSpeedBonus" -> item.copy(attackSpeedBonus = item.attackSpeedBonus + value / 100f)
            else -> item
        }
    }

    return item
}


fun getBonusCount(rarity: ItemRarity): Int = when (rarity) {
    ItemRarity.COMMON -> 0
    ItemRarity.INCOMMON -> 1
    ItemRarity.RARE -> 2
    ItemRarity.MYTHICAL -> 3
    ItemRarity.LEGENDARY -> 4
}

fun getPossibleBonuses(type: DataItem): List<String> = when (type) {
    DataItem.TATTOO -> listOf("attackSpeedBonus")
    else -> listOf("lifestealBonus", "criticalChanceBonus", "movementSpeedBonus", "criticalDamageBonus")
}

fun rollBonus(type: DataItem, rarity: ItemRarity): List<Pair<String, Int>> {
    val bonusCount = getBonusCount(rarity)
    if (bonusCount == 0) return emptyList()

    val possibleBonuses = getPossibleBonuses(type)
    val bonusRanges = bonusRangesByRarity[rarity] ?: return emptyList()
    val bonuses = mutableListOf<Pair<String, Int>>()

    // Para INCOMMON, não repetir bônus (mas só tem 1, então não repete mesmo)
    if (rarity == ItemRarity.INCOMMON) {
        val bonusType = possibleBonuses.random()
        val range = bonusRanges[bonusType] ?: (1..1)
        bonuses.add(bonusType to range.random())
        return bonuses
    }

    // Para RARE, MYTHICAL, LEGENDARY: pode repetir (inclusive TATTOO pode vir vários attackSpeedBonus)
    repeat(bonusCount) {
        val bonusType = possibleBonuses.random()
        val range = bonusRanges[bonusType] ?: (1..1)
        bonuses.add(bonusType to range.random())
    }
    return bonuses
}

fun generateSpecificItem(type: DataItem, grade: ItemGrade, rarity: ItemRarity): Item {
    val weaponType = if (type == DataItem.WEAPON) WeaponType.values().random() else null
    val iconName = generateSpriteName(type, weaponType, rarity)
    val iconRes = getIconResByName(iconName)
    val mainValue = when (type) {
        DataItem.BOOTS, DataItem.TATTOO -> mainBonusRanges[rarity]?.get(type)?.random() ?: 0
        else -> gradeStatusRanges[grade]?.random() ?: 0
    }
    var item = Item(
        id = (0..9999999).random(),
        name = "",
        iconName = iconName,
        iconRes = iconRes,
        type = type,
        weaponType = weaponType,
        grade = grade,
        rarity = rarity,
        description = ""
    )
    item = when (type) {
        DataItem.HELMET, DataItem.CHEST, DataItem.LEGS -> item.copy(defenseBonus = mainValue)
        DataItem.WEAPON -> item.copy(attackBonus = mainValue)
        DataItem.BOOTS -> item.copy(movementSpeedBonus = mainValue / 100f)
        DataItem.TATTOO -> item.copy(attackSpeedBonus = mainValue / 100f)
    }
    // Sorteia bônus conforme a nova regra
    val bonuses = rollBonus(type, rarity)
    for ((attr, value) in bonuses) {
        item = when (attr) {
            "lifestealBonus" -> item.copy(lifestealBonus = item.lifestealBonus + value / 100f)
            "criticalChanceBonus" -> item.copy(criticalChanceBonus = item.criticalChanceBonus + value / 100f)
            "movementSpeedBonus" -> item.copy(movementSpeedBonus = item.movementSpeedBonus + value / 100f)
            "criticalDamageBonus" -> item.copy(criticalDamageBonus = item.criticalDamageBonus + value / 100f)
            "attackSpeedBonus" -> item.copy(attackSpeedBonus = item.attackSpeedBonus + value / 100f)
            else -> item
        }
    }
    return item
}


// 8. EXEMPLO DE USO
/*
fun main() {
    repeat(10) {
        val item = generateDroppedItem(Random.nextInt(1, 80))
        println(item)
    }
}
*/
