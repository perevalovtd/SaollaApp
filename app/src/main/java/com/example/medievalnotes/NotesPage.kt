package com.example.medievalnotes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.util.Locale
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon


fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
fun IconButtonSkipBackward(onClick: () -> Unit) {
    // (1) Иконка из drawable resources
    // Можно через painterResource(R.drawable.ic_skip_back)
    val iconPainter = painterResource(R.drawable.ic_skip_back)

    // (2) Размер иконки, например 48.dp
    val iconSize = 48.dp

    // (3) Сам Composable
    Box(
        modifier = Modifier
            .size(iconSize)
            .clickable { onClick() }
    ) {
        // Рисуем саму иконку
        Icon(
            painter = iconPainter,
            contentDescription = "Skip -5 sec",
            tint = Color.White, // цвет иконки (можно менять под темную тему)
            modifier = Modifier.fillMaxSize() // заполнить весь Box
        )
    }

}
@Composable
fun IconButtonSkipForward(onClick: () -> Unit) {
    val iconPainter = painterResource(R.drawable.ic_skip_forward)
    val iconSize = 48.dp

    Box(
        modifier = Modifier
            .size(iconSize)
            .clickable { onClick() }
    ) {

        Icon(
            painter = iconPainter,
            contentDescription = "Skip +5 sec",
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun NotesPage(
    navController: NavHostController,
    vm: MainViewModel
) {
    // 1) Получаем текущую Activity через LocalContext
    // Получаем Activity безопасно
    val activity = LocalContext.current.findActivity()

    // 2) Устанавливаем/снимаем флаг KEEP_SCREEN_ON в DisposableEffect
    DisposableEffect(Unit) {
        // При входе
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // При выходе
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // 2) Получаем Vibrator
    val context = LocalContext.current
    val vibrator = remember {
        // Вариант для API < 23 тоже будет работать,
        // если vibrator != null, используем vibrator.vibrate(...)
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }


    // 1) Храним последнее зафиксированное время (чтобы понять, пошли ли назад)
    val lastRenderedTime = remember { mutableStateOf(0f) }
    // Запоминаем, для каких `timeSec` уже была вибрация:
    val vibratedSet = remember { mutableSetOf<Float>() }

    // Вариант A: делаем это прямо в Canvas-блоке (или после него).
    // Но "каждый кадр" вы можете вызывать логику:
    val songInfo = vm.currentSongInfo
    val currentTime = vm.currentTime

    // Здесь можно просто сделать:
    LaunchedEffect(currentTime) {
        // Проверка: если время пошло вспять (отмотка назад)
        if (currentTime < lastRenderedTime.value) {
            // Очистим vibratedSet, чтобы ноты снова могли вибрировать
            vibratedSet.clear()
        }
        // Проверяем все events
        songInfo?.events?.forEach { event ->
            val tSec = event.timeSec
            val triggerTime = (tSec - 0.075f).coerceAtLeast(0f)
            if (currentTime >= triggerTime && tSec !in vibratedSet) {
                // Вибрация
                if (vm.vibrationOn) {
                    vibrateOnce(vibrator)
                }
                vibratedSet.add(tSec)
            }
        }
        // Обновляем "предыдущее" время
        lastRenderedTime.value = currentTime
    }
    Box(
        modifier=Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val config = LocalConfiguration.current
        val screenWidthDp = config.screenWidthDp
        val screenHeightDp = config.screenHeightDp

        val screenWidthPx = with(LocalDensity.current){ screenWidthDp.dp.toPx() }
        val screenHeightPx = with(LocalDensity.current){ screenHeightDp.dp.toPx() }

        val notesToDraw = vm.getNotesToDraw(screenWidthPx, screenHeightPx)

        Canvas(modifier=Modifier.fillMaxSize()){
            for(note in notesToDraw){
                drawRect(
                    color=Color.White,
                    topLeft= androidx.compose.ui.geometry.Offset(note.x, note.top),
                    size= androidx.compose.ui.geometry.Size(note.width, note.height)
                )

            }
        }


        // === (2) «Заливка» сверху. Допустим, хотим закрыть 100dp высоты. ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)                // <-- высота заливки
                .background(Color.DarkGray)    // <-- любой вам нужный цвет
        )


        val timeText = String.format(Locale.US, "%.1f s", vm.currentTime)
        Text(
            text= timeText,
            color=Color.White,
            modifier=Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )

        // Кнопка Stop
        Column(
            modifier=Modifier
                .align(Alignment.TopEnd)
                .padding(top=7.dp, end=8.dp)
        ){
            Button(
                onClick={
                    vm.stopPlaying()
                    navController.popBackStack("page1", inclusive=false)
                },
                colors=ButtonDefaults.buttonColors(
                    containerColor=Color.White,
                    contentColor=Color.Black
                )
            ){
                Text("Back")
            }
        }

        val songTitle = vm.currentSongInfo?.title ?: "No song loaded"
        Text(
            text= songTitle,
            color=Color.White,
            modifier=Modifier
                .align(Alignment.TopCenter)
                .padding(8.dp)
        )

        // (2) Ряд из двух иконок (слева "back", справа "forward"):
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // Левая колонка: иконка + подпись "-5sec"
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButtonSkipBackward {
                    vm.seekBackward5sec()
                }
                Text(
                    text = "-5sec",
                    color = Color.White,
                    modifier = Modifier.offset(y = (-10).dp)
                )
            }

            // Правая колонка: иконка + подпись "+5sec"
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButtonSkipForward {
                    vm.seekForward5sec()
                }
                Text(
                    text = "+5sec",
                    color = Color.White,
                    modifier = Modifier.offset(y = (-10).dp)
                )
            }
        }
    }
}

/**
 * Пример функции: вызвать лёгкую вибрацию один раз ~50мс.
 */
private fun vibrateOnce(vibrator: Vibrator?) {
    if (vibrator == null) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Android O+:
        val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    } else {
        // До API 26: vibrate(milliseconds)
        vibrator.vibrate(50)  // 50мс
    }
}