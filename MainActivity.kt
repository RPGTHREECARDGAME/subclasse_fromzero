package com.example.subclasse_fromzero

import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SubClasseFromZeroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameApp()
        }
    }
}

@Composable
fun GameApp() {
    val gameViewModel: GameViewModel = viewModel()
    val rpgCharacter = remember { RPGCharacter(name = "Jogador", level = 1) } // posição será definida em outro lugar

    SubClasseFromZeroTheme {
        if (gameViewModel.isDebugMode) {
            InventoryScreen(
                gameViewModel = gameViewModel,
                character = rpgCharacter
            )
        } else {
            MapScreen(
                gameViewModel = gameViewModel,
                rpgCharacter = rpgCharacter
            )
        }
    }
}


