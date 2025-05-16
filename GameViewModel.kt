package com.example.subclasse_fromzero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class GameViewModel : ViewModel() {

    var isDebugMode by mutableStateOf(false) // controla modo debug
    var inventory by mutableStateOf(Inventory(slots = 24))
        private set

    init {
        viewModelScope.launch {
            fillInventory()
        }
    }

    fun fillInventory() {
        if (isDebugMode) {
            fillInventoryWithAllItemsForDebug()
        } else {
            fillInventoryNormal()
        }
    }

    private fun fillInventoryWithAllItemsForDebug() {
        val allItems = mutableListOf<Item>()
        val slots = 24
        repeat(slots) {
            val fakeLevel = (1..80).random()
            val item = generateDroppedItem(fakeLevel)
            allItems.add(item)
        }
        inventory = Inventory(slots = slots, items = allItems)
    }

    private fun fillInventoryNormal() {
        val allItems = mutableListOf<Item>()
        val slots = 24
        repeat(slots) {
            val fakeLevel = (1..80).random()
            val item = generateDroppedItem(fakeLevel)
            allItems.add(item)
        }
        inventory = Inventory(slots = slots, items = allItems)
    }

    fun toggleDebugMode() {
        isDebugMode = !isDebugMode
        fillInventory()
    }
}
