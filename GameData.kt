package com.example.subclasse_fromzero

object GameData {
    // Valores base
    const val BASE_HEALTH = 100
    const val BASE_ATTACK = 10
    const val BASE_DEFENSE = 5
    const val BASE_CRIT_RATE = 0.0f
    const val BASE_CRIT_DAMAGE = 1.5f
    const val BASE_ATTACK_SPEED = 1.43f   // ataques por segundo
    const val BASE_MOVEMENT_SPEED = 220f // Tibia padrão
    const val BASE_LIFESTEAL = 0.0f

    // Progressão sem limite de nível
    fun getProgression(level: Int): Progression {
        val maxHealth = BASE_HEALTH + (level - 1) * 16
        val attack = BASE_ATTACK + (level - 1) * 3
        val defense = BASE_DEFENSE + (level - 1) * 2

        // Fórmula de speed estilo Tibia
        val movementSpeed = BASE_MOVEMENT_SPEED + 2f * (level - 1)

        // Attack speed: +10% a cada 10 níveis, multiplicativo
        val attackSpeedMultiplier = 1 + ((level - 1) / 10) * 0.10f
        val attacksPerSecond = BASE_ATTACK_SPEED * attackSpeedMultiplier

        // Experiência para o próximo nível (Tibia * 5)
        val maxExperience = if (level <= 2) {
            100 * 5 // Níveis 1 e 2, experiência fixa
        } else {
            (250 * (level - 1) * (level - 2))
        }

        return Progression(
            maxExperience = maxExperience,
            maxHealth = maxHealth,
            attack = attack,
            defense = defense,
            critRate = BASE_CRIT_RATE,
            critDamage = BASE_CRIT_DAMAGE,
            attacksPerSecond = attacksPerSecond,
            movementSpeed = movementSpeed,
            lifesteal = BASE_LIFESTEAL
        )
    }
}

data class Progression(
    val maxExperience: Int,
    val maxHealth: Int,
    val attack: Int,
    val defense: Int,
    val critRate: Float,
    val critDamage: Float,
    val attacksPerSecond: Float,
    val movementSpeed: Float,
    val lifesteal: Float
)

