package com.example.subclasse_fromzero

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun InventoryScreen(gameViewModel: GameViewModel,character: RPGCharacter) {

    fun formatItemName(item: Item): String {
        val itemName = item.type.name.lowercase().replaceFirstChar { it.uppercase() }
        val rarity = item.rarity.name.lowercase().replaceFirstChar { it.uppercase() }
        return if (item.grade == ItemGrade.NO_GRADE) {
            "$itemName, $rarity"
        } else {
            val gradeLetter = item.grade.name.first()
            "$itemName, $gradeLetter, $rarity"
        }
    }
    var showInventory by remember { mutableStateOf(false) }
    val inventoryIconSize = 60.dp
    val inventoryIconOffsetX = (-5).dp
    val inventoryIconOffsetY = (25).dp
    val inventory = gameViewModel.inventory
    var selectedItem by remember { mutableStateOf<Item?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.inventory_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        CharacterStatusPanelSimple(
            character = character,
            fontSize = 13,
            leftX = 40.dp,
            rightX = 235.dp,
            y = 355.dp
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 0.dp, y = 438.dp)
                .padding(0.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(-34.dp)
        ) {
            items(inventory.items) { item ->
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .padding(1.dp)
                        .clickable { selectedItem = item }
                ) {
                    Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        selectedItem?.let { item ->
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart) // posiciona no canto inferior esquerdo
                    .offset(x = 16.dp, y = 10.dp) // ajuste manual direto aqui do eixo X e Y
                    .background(Color(0x00222222), shape = RoundedCornerShape(12.dp))
                    .padding(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = item.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 18.sp,
                        color = getRarityColor(item.rarity),
                        fontWeight = FontWeight.Bold
                    )
                    if (item.grade != ItemGrade.NO_GRADE) {
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = formatGradeShort(item.grade),
                            fontSize = 18.sp,
                            color = getGradeColor(item.grade),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (item.attackBonus > 0) {
                    Text(text = "+${item.attackBonus} attack", color = Color.White)
                }
                if (item.defenseBonus > 0) {
                    Text(text = "+${item.defenseBonus} defense", color = Color.White)
                }
                if (item.attackSpeedBonus != 0f) {
                    Text(text = "+${(item.attackSpeedBonus * 100).toInt()}% atk speed", color = Color.White)
                }
                if (item.criticalChanceBonus != 0f) {
                    Text(text = "+${(item.criticalChanceBonus * 100).toInt()}% crit chance", color = Color.Yellow)
                }
                if (item.criticalDamageBonus != 0f) {
                    Text(text = "+${(item.criticalDamageBonus * 100).toInt()}% crit dmg", color = Color.Yellow)
                }
                if (item.lifestealBonus != 0f) {
                    Text(text = "+${(item.lifestealBonus * 100).toInt()}% lifesteal", color = Color.Cyan)
                }
                if (item.movementSpeedBonus != 0f) {
                    Text(text = "+${(item.movementSpeedBonus * 100).toInt()}% move speed", color = Color.Green)
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (item.description.isNotBlank()) {
                    //Text(text = item.description, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                //Button(onClick = { selectedItem = null }) {
                    //Text("Fechar")
                //}
            }
        }
        Image(
            painter = painterResource(id = R.drawable.inventory_icon),
            contentDescription = "Ícone do Inventário",
            modifier = Modifier
                .size(inventoryIconSize)
                .align(Alignment.TopEnd)
                .offset(x = inventoryIconOffsetX, y = inventoryIconOffsetY)
                .clickable {
                    showInventory = false
                    // chama o callback para fechar o inventário
                }
        )

    }
}

@Composable
fun CharacterStatusPanelSimple(
    character: RPGCharacter,
    fontSize: Int = 14,
    leftX: Dp = 40.dp,
    rightX: Dp = 370.dp,
    y: Dp = 650.dp
) {
    val fontFamily = FontFamily.Default
    val corBase = Color.White
    val corBonusItem = Color(0xFFFFC107) // Amarelo

    // Usando diretamente os valores do personagem, sem somar itens
    val atkBase = character.attack
    val defBase = character.defense
    val spdBase = character.movementSpeed
    val hpBase = character.maxHealth
    val crtDmgBase = character.critDamage
    val crtRteBase = character.critRate
    val atkSpdBase = character.attacksPerSecond
    val lifestealBase = character.lifesteal

    // Como não tem bonus de item, itemBonus = 0
    val zeroInt = 0
    val zeroFloat = 0f

    Box(modifier = Modifier.fillMaxSize()) {
        // Nível do personagem
        Text(
            text = "Level: ${character.level}",
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            color = corBonusItem,
            modifier = Modifier.offset(x = 175.dp, y = 105.dp),
        )

        // Coluna esquerda
        Column(
            modifier = Modifier
                .offset(x = leftX, y = y)
                .width(120.dp),
            verticalArrangement = Arrangement.spacedBy(-8.dp)
        ) {
            StatusLineSimple("atk", atkBase, zeroInt, fontSize, fontFamily, corBase, corBonusItem)
            StatusLineSimple("def", defBase, zeroInt, fontSize, fontFamily, corBase, corBonusItem)
            StatusLineSimple("spd", spdBase, zeroFloat, fontSize, fontFamily, corBase, corBonusItem, isFloat = true)
            StatusLineSimple("hp", hpBase, zeroInt, fontSize, fontFamily, corBase, corBonusItem)
        }

        // Coluna direita
        Column(
            modifier = Modifier
                .offset(x = rightX, y = y)
                .width(140.dp),
            verticalArrangement = Arrangement.spacedBy(-8.dp)
        ) {
            StatusLineSimple("crt dmg", crtDmgBase, zeroFloat, fontSize, fontFamily, corBase, corBonusItem, isFloat = true, isPercent = true)
            StatusLineSimple("crt rte", crtRteBase, zeroFloat, fontSize, fontFamily, corBase, corBonusItem, isFloat = true, isPercent = true)
            StatusLineSimple("atk spd", atkSpdBase, zeroFloat, fontSize, fontFamily, corBase, corBonusItem, isFloat = true)
            StatusLineSimple("vamp", lifestealBase, zeroFloat, fontSize, fontFamily, corBase, corBonusItem, isFloat = true, isPercent = true)
        }
    }
}

@Composable
fun StatusLineSimple(
    label: String,
    base: Number,
    itemBonus: Number,
    fontSize: Int,
    fontFamily: FontFamily,
    baseColor: Color,
    itemColor: Color,
    isFloat: Boolean = false,
    isPercent: Boolean = false
) {
    Row {
        Text(
            text = "$label: ",
            fontFamily = fontFamily,
            fontSize = fontSize.sp,
            color = baseColor
        )
        Text(
            text = if (isFloat) String.format("%.2f", base.toFloat()) else base.toString(),
            fontFamily = fontFamily,
            fontSize = fontSize.sp,
            color = baseColor
        )
        if (itemBonus.toFloat() != 0f) {
            Text(
                text = " +${if (isFloat) String.format("%.2f", itemBonus.toFloat()) else itemBonus}${if (isPercent) "%" else ""}",
                fontFamily = fontFamily,
                fontSize = fontSize.sp,
                color = itemColor
            )
        }
    }
}

// Funções auxiliares para cores e formatação

fun getRarityColor(rarity: ItemRarity): Color = when (rarity) {
    ItemRarity.COMMON -> Color(0xFFAAAAAA)
    ItemRarity.INCOMMON -> Color(0xFF00B400)
    ItemRarity.RARE -> Color(0xFF03A9F4)
    ItemRarity.MYTHICAL -> Color(0xFFA11DA1)
    ItemRarity.LEGENDARY -> Color(0xFFD5B402)
}

fun getGradeColor(grade: ItemGrade): Color = Color(0xFFFFD700)

fun formatGradeShort(grade: ItemGrade): String = when (grade) {
    ItemGrade.NO_GRADE -> ""
    else -> grade.name.first().toString()
}
