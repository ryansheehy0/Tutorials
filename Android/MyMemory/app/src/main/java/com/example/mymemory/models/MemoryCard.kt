package com.example.mymemory.models

data class MemoryCard(
    val identifier: Int, //image for memory card
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)