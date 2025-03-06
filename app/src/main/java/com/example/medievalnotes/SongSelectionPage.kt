package com.example.medievalnotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext



@Composable
fun SongSelectionPage(
    navController: NavHostController,
    vm: MainViewModel
) {
    val listState = rememberLazyListState()

    // Цвет фона для «Songs:»
    val songsTitleBg = if (vm.isDarkTheme) Color(0xff1A1A1A) else Color(0xFFE73B85)

    val bgColor = if (vm.isDarkTheme) Color.Black else Color(0xFFEAE2D5)
    val textColor = if (vm.isDarkTheme) Color.White else Color.Black
    val textColorsongs = Color.White
    val btnContainer = if (vm.isDarkTheme) Color.DarkGray else Color(0xFFEEFF8F)
    val btnContent = if (vm.isDarkTheme) Color.White else Color.Black

    // scroll line colors
    val trackColor = if (vm.isDarkTheme) Color.DarkGray else Color(0xFF91F5D8)
    val handleColor = if (vm.isDarkTheme) Color.LightGray else Color(0xFF96B5D2)

    // Preview:
    val previewButtonContainer = if (vm.isDarkTheme) {
        Color(0xff1A1A1A)             // Тёмная тема
    } else {
        Color(0xFFFF8D08)         // Светлая тема => #ff8d08
    }
    val previewButtonContent = if (vm.isDarkTheme) {
        Color.White
    } else {
        Color.White               // На оранжевом #ff8d08 — белый текст
    }

    // Back:
    val backButtonContainer = if (vm.isDarkTheme) {
        Color(0xff1A1A1A)            // Тёмная тема
    } else {
        Color(0xFF5C3926)         // Светлая => #5c3926
    }
    val backButtonContent = if (vm.isDarkTheme) {
        Color.White
    } else {
        Color.White               // На коричневом #5c3926 — белый текст
    }


    // Добавим DisposableEffect, который вызовется один раз при «отмонтировании» страницы
    DisposableEffect(Unit) {
        // onDispose вызывается, когда мы уходим со стр.2 (navigation на page1 или page3)
        onDispose {
            // Если пользователь действительно уходит с экрана (а не просто свернул приложение),
            // мы хотим остановить music.
            vm.stopPlaying()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {

        // Слой, где всё выравниваем по вертикали
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {


            // (1) "Шапка" с надписью "Songs:" в розовом фоне (#E73B85),
            //     закреплённая сверху
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)           // например, фиксированная высота
                    .background(songsTitleBg)
            ) {
                // Внутри розового Box — надпись
                Text(
                    text = "Songs:",
                    color = textColorsongs,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(start = 16.dp)
                )
            }


            // (B) — Остальная часть экрана: список песен + скроллбар + кнопки «Preview / Back»
            Box(
                modifier = Modifier
                    .weight(1f)             // занять всё оставшееся место
                    .fillMaxWidth()
            ) {
                // -- сам список (LazyColumn)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 20.dp),  // чтобы было место под скролл
                    state = listState
                ) {

                    items(vm.songList.size) { index ->
                        val songItem = vm.songList[index]
                        val isSelected = (index == vm.selectedSongIndex)
                        val bg = if (isSelected) btnContainer else Color.Transparent

                        val context = LocalContext.current

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .clickable {
                                    vm.selectSong(context, index)
                                }
                                .padding(8.dp)
                        ) {
                            Text(songItem.title, color = textColor)
                        }
                    }
                }


                // -- Кнопки в правом нижнем углу
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)  // Расстояние между кнопками
                ) {
                    // Кнопка Play (на стр.2)
                    Button(
                        onClick = {
                            vm.togglePlayOnPage2(navController.context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = previewButtonContainer,
                            contentColor = previewButtonContent
                        )
                    ) {
                        Text("Preview")
                        // при повторном нажатии,
                        // если песня playing => pause,
                        // если paused => resume,
                        // если другая песня => start c начала
                    }

                    // Кнопка Back (ниже)
                    Button(
                        onClick = {
                            // Останавливаем музыку, затем уходим на стр.1
                            vm.stopMusic()
                            navController.popBackStack("page1", false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = backButtonContainer,
                            contentColor = backButtonContent
                        )
                    ) {
                        Text("Back")
                    }
                }

                // -- scroll line
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                val total = vm.songList.size

                val avgSize = if (visibleItems.isNotEmpty()) {
                    visibleItems.sumOf { it.size.toDouble() }.toFloat() / visibleItems.size
                } else 1f

                val vpHeightPx = layoutInfo.viewportSize.height.toFloat().coerceAtLeast(1f)
                val visibleCount = vpHeightPx / avgSize

                val firstItem = visibleItems.firstOrNull()
                val scrolledPx = if (firstItem != null) {
                    val offsetPx = if (firstItem.offset < 0) -firstItem.offset.toFloat()
                    else firstItem.offset.toFloat()
                    (firstItem.index * avgSize) + offsetPx
                } else 0f

                val maxScrollPx = (total - visibleCount) * avgSize
                val fraction =
                    if (maxScrollPx > 0f) (scrolledPx / maxScrollPx).coerceIn(0f, 1f) else 0f

                val fractionVisible =
                    if (total > 0) (visibleCount / total.toFloat()).coerceIn(0f, 1f)
                    else 1f

                val trackHeightPx = 300f
                val handleHeightPx = (trackHeightPx * fractionVisible).coerceAtLeast(20f)
                val handleTopPx = fraction * (trackHeightPx - handleHeightPx)

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(8.dp)
                        .height(300.dp)
                        .background(trackColor)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = handleTopPx.dp)
                            .fillMaxWidth()
                            .height(handleHeightPx.dp)
                            .background(handleColor)
                    )
                }
            }
        }
    }
}


