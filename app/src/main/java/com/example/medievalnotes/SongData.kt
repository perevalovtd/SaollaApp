package com.example.medievalnotes

data class SongInfo(
    val title: String,
    val mp3Name: String,        // <-- Добавлено новое поле
    val durationSec: Float,
    val minNote: Int,
    val maxNote: Int,
    val eventCount: Int,
    val events: List<Event>
)

data class Event(
    val timeSec: Float,
    val notes: List<Int>
)
