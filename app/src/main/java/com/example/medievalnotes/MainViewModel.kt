package com.example.medievalnotes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import androidx.compose.runtime.*
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject



enum class PlaybackMode { APP, GUITAR }

class MainViewModel : ViewModel() {


    private val PREFS_NAME = "app_config"       // название файла SharedPreferences
    private val PREFS_KEY_JSON = "config_json"  // ключ, где хранить JSON

    /**
     * Сохранить текущее состояние (Dark/Light, номер песни, темп, режим Playback, DemoMode)
     * в JSON и записать в SharedPreferences.
     */
    fun saveConfigToFile(context: Context) {
        // 1) Формируем JSON-объект
        val jsonObj = JSONObject().apply {
            put("isDarkTheme", isDarkTheme)
            put("selectedSongIndex", selectedSongIndex)
            put("tempo", tempo)
            put("demoMode", demoMode)
            put("startGuitar", startGuitar)
            put("musicOnPhone", musicOnPhone)
            put("vibrationOn", vibrationOn)
        }

        // 2) Получаем SharedPreferences
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 3) Пишем JSON строку
        prefs.edit()
            .putString(PREFS_KEY_JSON, jsonObj.toString())
            .apply()
    }

    /**
     * Загрузить JSON из SharedPreferences, распарсить и применить к полям
     */
    fun loadConfigFromFile(context: Context) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Читаем строку JSON
        val jsonString = prefs.getString(PREFS_KEY_JSON, null) ?: return

        try {
            val jsonObj = JSONObject(jsonString)

            isDarkTheme = jsonObj.optBoolean("isDarkTheme", false)
            selectedSongIndex = jsonObj.optInt("selectedSongIndex", -1)
            tempo = jsonObj.optDouble("tempo", 1.0).toFloat()
            startGuitar = jsonObj.optBoolean("startGuitar", false)
            musicOnPhone = jsonObj.optBoolean("musicOnPhone", true)
            demoMode = jsonObj.optBoolean("demoMode", false)
            vibrationOn = jsonObj.optBoolean("vibrationOn", true)

        } catch(e: Exception) {
            e.printStackTrace()
            Log.e("Config", "Error parsing config JSON: ${e.message}")
        }
    }


    var startGuitar by mutableStateOf(true)
        private set

    var musicOnPhone by mutableStateOf(true)
        private set

    fun updateStartGuitar(context: Context, value: Boolean) {
        startGuitar = value
        saveConfigToFile(context) // сохраним в SharedPreferences (JSON)
    }

    fun updateMusicOnPhone(context: Context, value: Boolean) {
        musicOnPhone = value
        saveConfigToFile(context)
    }


    var songList by mutableStateOf<List<SongItem>>(emptyList())
        private set

    var selectedSongIndex by mutableStateOf(-1)
        private set

    var currentSongInfo by mutableStateOf<SongInfo?>(null)
        private set

    var tempo by mutableStateOf(1.0f)
        private set

    var demoMode by mutableStateOf(false)
        private set

    var currentTime by mutableStateOf(0f)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var isDarkTheme by mutableStateOf(false)
        private set
    var vibrationOn by mutableStateOf(true)
        private set

    // ExoPlayer
    private var exoPlayer: ExoPlayer? = null
    private var currentlyLoadedIndex: Int? = null

    private var statsJob: Job? = null

    // Для отправки статистики (IP, порт)
    private val statsIpStr = "192.168.4.1"
    private val statsPort = 12345


    // mp3Names from arrays.xml
    //private var mp3Names: Array<String> = emptyArray()

    init {
        // Запускаем job, которое каждые 5 сек посылает статистику,
        // пока ViewModel жив (пока приложение открыто).
        statsJob = viewModelScope.launch {
            while (true) {
                delay(5000)
                sendUdpStatistics()  // вызовем новую функцию (см. ниже)
            }
        }
    }





    override fun onCleared() {
        super.onCleared()
        // Освобождаем плеер
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }

    fun loadSongList(context: Context) {
        // 1) Считываем txt из assets
        val list = getSongListFromAssets(context)
        songList = list
        if (list.isNotEmpty() && selectedSongIndex == -1) {
            selectedSongIndex = 0
        }
        // 2) Считываем массив имён mp3
        //mp3Names = context.resources.getStringArray(R.array.my_songs_array)
    }

    fun updateVibrationOn(context: Context, value: Boolean) {
        vibrationOn = value
        saveConfigToFile(context) // сохранить новое значение в JSON
    }

    fun selectSong(context: Context, index: Int) {
        selectedSongIndex = index
        saveConfigToFile(context)
        sendUdpStatistics()
    }

    fun toggleDarkTheme(context: Context) {
        isDarkTheme = !isDarkTheme
        saveConfigToFile(context) // сохранить в файл
    }

    fun updateTempo(context: Context, value: Float) {
        tempo = value
        // Меняем скорость, если плеер активен
        exoPlayer?.let { p ->
            val params = PlaybackParameters(tempo, 1f)
            p.playbackParameters = params
        }
        sendUdpStatistics()
        saveConfigToFile(context)
    }

    fun updateDemoMode(context: Context, value: Boolean) {
        demoMode = value
        sendUdpStatistics()
        saveConfigToFile(context)
    }

    fun getSelectedFileName(): String? {
        if (selectedSongIndex !in songList.indices) return null
        return songList[selectedSongIndex].fileName
    }

    fun getSelectedTitle(): String {
        if (selectedSongIndex !in songList.indices) return "No songs"
        return songList[selectedSongIndex].title
    }

    /**
     * Запуск нот + музыки (использ. на стр.1 при нажатии Play)
     */
    fun startPlaying(context: Context) {
        val idx = selectedSongIndex
        if (idx !in songList.indices) return

        // 1) Загружаем .txt => currentSongInfo => ноты
        val fileName = songList[idx].fileName
        currentSongInfo = readSongInfoFromAssets(context, fileName)
        currentTime = 0f
        isPlaying = true

        startMusic(context, idx, fromPage2 = false)


        // 3) Если стоит флажок startGuitar => отправляем UDP-сообщение "play"
        if (startGuitar) {
            sendUdpPlayMessage()
        }

        sendUdpStatistics()
    }



    /**
     * Перемотка вперёд на +5 секунд.
     * Если выходит за пределы длительности песни => идём в конец.
     */
    fun seekForward5sec() {
        val player = exoPlayer ?: return
        val durationMs = player.duration // длительность трека в мс (или C.TIME_UNSET, если неизвестно)
        if (durationMs == com.google.android.exoplayer2.C.TIME_UNSET) {
            // Неизвестна длительность - просто прибавим 5 сек
            val newPos = player.currentPosition + 5000L
            player.seekTo(newPos)
            return
        }
        // Иначе известно время
        val current = player.currentPosition
        val target = current + 5000L
        val clamped = minOf(target, durationMs) // ограничиваем
        player.seekTo(clamped)
    }

    /**
     * Перемотка назад на -5 секунд.
     * Если выходит за пределы начала => идём в 0.
     */
    fun seekBackward5sec() {
        val player = exoPlayer ?: return
        val current = player.currentPosition
        val target = current - 5000L
        val clamped = maxOf(0L, target)
        player.seekTo(clamped)
    }



    /**
     * Переключить «пауза/продолжить» (только на стр.3).
     * Если сейчас playing => поставим на паузу,
     * если пауза => продолжить с того же места.
     */
    fun togglePauseResume() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            // Пауза
            player.playWhenReady = false
            isPlaying = false
        } else {
            // Возобновить
            player.playWhenReady = true
            isPlaying = true
        }
    }

    fun togglePlayOnPage2PauseOnly() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.playWhenReady = false // пауза
            isPlaying = false
        }
        // если уже на паузе, ничего не делаем
    }


    /****
     * Отправляет статистику: "X Y Z"
     * X = (selectedSongIndex+1) (номер песни, 1-based)
     * Y = tempo * 100 (округлён до int)
     * Z = 1 если demoMode=true, иначе 0
     */
    private fun sendUdpStatistics() {
        val songNumber = (selectedSongIndex + 1).coerceAtLeast(1)
        val tempoPercent = (tempo * 100).toInt()
        val demoInt = if (demoMode) 1 else 0
        val message = "stat $songNumber $tempoPercent $demoInt"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inet = InetAddress.getByName(statsIpStr)
                val data = message.toByteArray()

                DatagramSocket().use { socket ->
                    val packet = DatagramPacket(data, data.size, inet, statsPort)
                    socket.send(packet)
                }
                Log.d("UDPStats", "Sent stats: '$message' to $statsIpStr:$statsPort")
            } catch(e: Exception) {
                Log.e("UDPStats", "UDP stats error: ${e.message}")
            }
        }
    }


    private fun sendUdpPlayMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val ipStr = "192.168.4.1"
                val inet = InetAddress.getByName(ipStr)

                // random port
                val port = 12345

                val message = "play"
                val data = message.toByteArray()

                DatagramSocket().use { socket ->
                    val packet = DatagramPacket(data, data.size, inet, port)
                    socket.send(packet)
                }
                Log.d("UDPTest", "Sent 'play' to $ipStr:$port")
            } catch(e: Exception) {
                Log.e("UDPTest", "UDP error: ${e.message}")
            }
        }
    }

    fun stopPlaying() {
        isPlaying = false
        currentTime = 0f
        stopMusic()
    }

    /**
     * На странице 2 (SongSelectionPage) при нажатии кнопки "Play".
     * Логика:
     *  - Если пользователь выбрал другую песню (idx != currentlyLoadedIndex):
     *    -> Останавливаем предыдущий плеер, загружаем новую песню, ставим play с начала (pos=0).
     *  - Если та же песня:
     *    -> Если сейчас играет: пауза
     *    -> Если сейчас на паузе: возобновляем (resume) с той же позиции
     */
    fun togglePlayOnPage2(context: Context) {
        val idx = selectedSongIndex
        if (idx !in songList.indices) return

        // Если нет загруженного плеера, или песня другая
        if (currentlyLoadedIndex == null || currentlyLoadedIndex != idx || exoPlayer == null) {
            // Значит, пользователь переключил песню
            // или плеер не был инициализирован => загружаем c нуля
            loadSongAndPlay(context, idx, true)
            isPlaying = true
            return
        }

        // Иначе (уже загружена та же песня) => пауза или resume
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            // Пауза (сохраняем позицию автоматически)
            player.playWhenReady = false
            isPlaying = false
        } else {
            // Продолжаем с того места, где остановились
            player.playWhenReady = true
            isPlaying = true
        }
    }

    /**
     * Загружает песню с индексом idx "с нуля", начинает проигрывать
     * (обнуление предыдущего плеера, создание нового)
     */
    private fun loadSongAndPlay(context: Context, idx: Int, isPreview: Boolean = false) {
        // 1) Останавливаем и освобождаем предыдущий
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null

        // 2) Загружаем SongInfo, берём mp3Name
        val info = readSongInfoFromAssets(context, songList[idx].fileName)
        currentSongInfo = info
        currentlyLoadedIndex = idx

        val mp3FullName = info.mp3Name  // например "song1.mp3"
        val resourceName = mp3FullName.substringBeforeLast(".")  // "song1"

        val rawId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (rawId == 0) {
            // не нашли mp3
            return
        }

        // 3) Создаём ExoPlayer
        val newPlayer = ExoPlayer.Builder(context).build()
        val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/$rawId")
        newPlayer.setMediaItem(mediaItem)
        newPlayer.prepare()

        // 4) Применяем громкость, скорость
        val effectiveTempo = if (isPreview) 1.0f else tempo
        val params = PlaybackParameters(effectiveTempo, 1f)
        newPlayer.playbackParameters = params
        newPlayer.volume = 0.8f

        // 5) Старт с нуля
        newPlayer.seekTo(0)  // на всякий случай
        newPlayer.playWhenReady = true

        exoPlayer = newPlayer
    }

    /**
     * Запускаем корутину, слушающую UDP порт 12345.
     * Если приходит "play" => handleUdpPlay
     * Если приходит "stop" => handleUdpStop
     */
    fun startUdpServer(
        context: Context,
        onNavigateToPage3: () -> Unit,
        onNavigateToPage1: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("UDPServer", "Starting server on port 12345")
                val socket = DatagramSocket(12345) // <-- слушаем порт 12345
                Log.d("UDPServer", "DatagramSocket created ok")
                val buffer = ByteArray(1024)

                while (true) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val msg = packet.data.decodeToString(
                        endIndex = packet.length
                    ).trim()

                    Log.d("UDPServer", "Received message: '$msg'")

                    when (msg.lowercase()) {
                        "play" -> {
                            // вызываем handleUdpPlay в главном потоке
                            withContext(Dispatchers.Main) {
                                handleUdpPlay(context) {
                                    onNavigateToPage3()
                                }
                            }
                        }
                        "stop" -> {
                            withContext(Dispatchers.Main) {
                                handleUdpStop {
                                    onNavigateToPage1()
                                }
                            }
                        }
                        else -> {
                            // ignore
                            Log.d("UDPServer", "Unknown message: '$msg'")
                        }
                    }
                }
            } catch(e: Exception) {
                Log.e("UDPServer", "UDP server error: ${e.message}")
            }
        }
    }




    /**
     * Общий метод запуска mp3.
     * fromPage2 = true означает, что запуск идет со страницы 2 (просто прослушка),
     * fromPage2 = false => запуск вместе с нотами (стр.1 -> стр.3).
     */
    private fun startMusic(context: Context, idx: Int, fromPage2: Boolean) {
        if (idx !in songList.indices) return

        // (1) Считываем .txt => SongInfo
        val info = readSongInfoFromAssets(context, songList[idx].fileName)

        // (2) Извлекаем имя mp3, напр. "song1.mp3"
        val mp3FullName = info.mp3Name
        // Убираем ".mp3", чтобы найти ресурс "song1"
        val resourceName = mp3FullName.substringBeforeLast(".")  // "song1"

        // (3) Остановим/освободим предыдущий ExoPlayer
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null

        // (4) Ищем ресурс
        val rawId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (rawId == 0) {
            // mp3 не найден в res/raw
            return
        }

        // (5) Создаём новый плеер
        val newPlayer = ExoPlayer.Builder(context).build()

        val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/$rawId")
        newPlayer.setMediaItem(mediaItem)
        newPlayer.prepare()

        // (6) Применяем громкость, скорость
        val volumeToSet = if (musicOnPhone) 0.8f else 0f
        newPlayer.volume = volumeToSet
        val params = PlaybackParameters(tempo, 1f)
        newPlayer.playbackParameters = params

        // (7) Запуск
        newPlayer.playWhenReady = true
        exoPlayer = newPlayer
        observePlayerPosition()
    }

    private fun observePlayerPosition() {
        viewModelScope.launch {
            while (true) {
                val p = exoPlayer
                if (p == null) {
                    break // плеер освобождён
                }
                // Если плеер существует — берём его текущую позицию (мс)
                val posMs = p.currentPosition
                // Переводим в секунды
                val posSec = posMs / 1000f

                // Обновляем currentTime
                // (Compose увидит изменение, т. к. currentTime — это var by mutableStateOf)
                currentTime = posSec

                // Если хотите ещё проверять isPlaying = (p.playWhenReady && p.playbackState == ...)
                // но обычно достаточно этого
                kotlinx.coroutines.delay(30)  // 50ms для плавности
            }
        }
    }


    fun stopMusic() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        currentlyLoadedIndex = null
    }

    // Ноты
    fun getNotesToDraw(screenWidthPx: Float, screenHeightPx: Float): List<NoteDrawInfo> {
        val info = currentSongInfo ?: return emptyList()
        val minN = info.minNote
        val maxN = info.maxNote
        val range = max(1, maxN - minN)

        val noteWidthPx = 30f
        val noteHeightPx = 50f
        val res = mutableListOf<NoteDrawInfo>()

        for (event in info.events) {
            val T = event.timeSec
            for (n in event.notes) {
                val progress = (currentTime - (T - 5f)) / 5f
                if (progress in 0f..1f) {
                    val x = (n - minN)/range.toFloat()*(screenWidthPx-noteWidthPx)
                    val top = progress*(screenHeightPx-noteHeightPx)
                    res.add(
                        NoteDrawInfo(
                            timeSec = T,
                            pitch = n,
                            x = x,
                            top = top,
                            width = noteWidthPx,
                            height = noteHeightPx
                        )
                    )
                }

            }
        }
        return res
    }

    /**
     * Вызывается, когда по UDP пришла команда "play"
     * Логика:
     *  - Если уже isPlaying => остановить и начать заново
     *    (останемся на странице с нотами, не трогаем навигацию)
     *  - Если не isPlaying => запустить, как будто нажали Play
     *    и попросить активити перейти на стр.3
     */
    fun handleUdpPlay(context: Context, onNavigateToPage3: () -> Unit) {
        if (isPlaying) {
            // Уже идёт воспроизведение => перезапустим с начала
            stopPlayingNoNavigation()
            startPlayingNoNavigation(context)
            // Остаёмся на странице нот (не выходим на стр.1),
            // значит никаких onNavigateToPage3 вызывать не нужно
        } else {
            // Если не играло => запустить, + перейти на стр.3
            startPlayingNoNavigation(context)
            onNavigateToPage3()
        }
    }

    /**
     * Вызывается, когда по UDP пришла команда "stop"
     * Логика:
     *  - Если сейчас isPlaying => остановить + перейти на стр.1
     *  - Иначе (не играет) => ничего не делать
     */
    fun handleUdpStop(onNavigateToPage1: () -> Unit) {
        if (isPlaying) {
            stopPlaying()
            // перейти на стр.1
            onNavigateToPage1()
        }
    }

    /**
     * Дополнительная функция: запускает ноты/музыку (как startPlaying),
     * НО без перехода на страницу 3. (сохраняем логику)
     */
    private fun startPlayingNoNavigation(context: Context) {
        val idx = selectedSongIndex
        if (idx !in songList.indices) return

        // Загружаем .txt => currentSongInfo => ноты
        val fileName = songList[idx].fileName
        currentSongInfo = readSongInfoFromAssets(context, fileName)
        currentTime = 0f
        isPlaying = true

        // Создаём + запускаем ExoPlayer
        startMusic(context, idx, fromPage2 = false)


    }

    /**
     * Останавливаем без навигации, но сбрасываем isPlaying,
     * currentTime = 0 (если нужно).
     */
    private fun stopPlayingNoNavigation() {
        isPlaying = false
        currentTime = 0f
        stopMusic()
    }

}

data class NoteDrawInfo(
    val timeSec: Float,      // <-- идентификатор времени ноты
    val pitch: Int,          // <-- или нота
    val x: Float,
    val top: Float,
    val width: Float,
    val height: Float
)
