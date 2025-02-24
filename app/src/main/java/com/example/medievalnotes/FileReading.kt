package com.example.medievalnotes

import android.content.Context
import java.io.IOException

data class SongItem(
    val fileName: String,
    val title: String
)

/**
 * Сканируем `assets`, находим все файлы, заканчивающиеся на ".txt".
 * Формируем список (SongItem).
 */
fun getSongListFromAssets(context: Context): List<SongItem> {
    return try {
        val allFiles = context.assets.list("")?.filter { it.endsWith(".txt") } ?: emptyList()
        allFiles.map { fileName ->
            val lines = context.assets.open(fileName).bufferedReader().use { it.readLines() }
            val title = if (lines.isNotEmpty()) lines[0] else "Unknown"
            SongItem(fileName, title)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        emptyList()
    }
}

/**
 * Полное чтение `.txt` => SongInfo
 * ТЕПЕРЬ:
 *   0: title
 *   1: mp3Name (например "song1.mp3")
 *   2: durationSec
 *   3: minNote
 *   4: maxNote
 *   5: eventCount
 *   6.. => события
 */
fun readSongInfoFromAssets(context: Context, fileName: String): SongInfo {
    val lines = context.assets.open(fileName).bufferedReader().use {
        it.readLines()
    }.filter { it.isNotBlank() }

    val title = lines[0]
    val mp3Name = lines[1]            // <-- ЭТО НОВО: 2-я строка в txt = mp3Name
    val durationSec = lines[2].toFloat()
    val minNote = lines[3].toInt()
    val maxNote = lines[4].toInt()
    val eventCount = lines[5].toInt()

    val events = mutableListOf<Event>()
    for (i in 0 until eventCount) {
        val idx = 6 + i
        val parts = lines[idx].split(" ")
        val timeSec = parts[0].toFloat()
        val notes = parts.drop(1).map { it.toInt() }
        events.add(Event(timeSec, notes))
    }

    return SongInfo(
        title = title,
        mp3Name = mp3Name,          // <-- Указываем mp3Name
        durationSec = durationSec,
        minNote = minNote,
        maxNote = maxNote,
        eventCount = eventCount,
        events = events
    )
}
