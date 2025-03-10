package com.example.medievalnotes

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.navigation.NavHostController
import kotlin.math.min


@Composable
fun MainPage(
    navController: NavHostController,
    vm: MainViewModel
) {
    // --- (1) Цвета для ФОНА (фон всего экрана) ---
    val backgroundColor = if (vm.isDarkTheme) {
        // (как было в dark theme)
        Color(0xff000000)
    } else {
        // (НОВО для LightTheme)
        Color(0xFFEAE2D5) // #eae2d5
    }

    // --- (2) Цвет текста ---
    val textColor = if (vm.isDarkTheme) {
        // Тёмная тема — текст белый
        Color.White
    } else {
        // Светлая тема
        Color.Black
    }

    // --- (3) Фон области «настроек» ---
    val settingsAreaBgColor = if (vm.isDarkTheme) {
        // (как было)
        Color(0xff1A1A1A)
    } else {
        // (НОВО для LightTheme) #eeff8f
        Color(0xFFEEFF8F)
    }

    // --- (4) Цвет текста внутри области «настроек» ---
    val settingsTextColor = if (vm.isDarkTheme) {
        // в тёмной теме пусть будет как прежде (White)
        Color.White
    } else {
        // (НОВО для LightTheme)
        Color.Black
    }

    // --- (5) Цвета кнопок Light/Dark (оставляем как было) ---
    val themeButtonText = if (vm.isDarkTheme) "Light" else "Dark"
    val themeButtonContainer = if (vm.isDarkTheme) Color.White else Color.Black
    val themeButtonContent = if (vm.isDarkTheme) Color.Black else Color.White

    // --- (6) Кнопка Song ---
    // В тёмной теме (dark) оставим как раньше (или какое-то своё),
    // а в светлой — #e73b85 + белый текст:
    val songButtonContainer = if (vm.isDarkTheme) {
        // (как раньше в dark)
        Color.DarkGray
    } else {
        // (НОВО для LightTheme) #e73b85
        Color(0xFFE73B85)
    }
    val songButtonContent = if (vm.isDarkTheme) {
        // (как раньше)
        Color.White
    } else {
        // (НОВО для LightTheme)
        Color.White // на розовом фоне белый текст
    }

    // --- (7) Кнопка Play ---
    // Аналогично: в dark теме старые цвета,
    // в light => фон #ff8d08, текст белый
    val playButtonContainer = if (vm.isDarkTheme) {
        // (как было в dark)
        Color.DarkGray
    } else {
        // (НОВО для LightTheme) #ff8d08
        Color(0xFFFF8D08)
    }
    val playButtonContent = if (vm.isDarkTheme) {
        // (как было)
        Color.White
    } else {
        // (НОВО для LightTheme)
        Color.White
    }

    // --- (8) Цвет checkBox (оставляем как было) ---
    val checkBoxBoxColor = if (vm.isDarkTheme) Color.DarkGray else Color(0xFF5C3926)
    val checkMarkColor = Color.White

    // --- (9) Цвета скроллбара (track/handle) (как было) ---
    val trackColor = if (vm.isDarkTheme) {
        Color(0xFF343434)
    } else {
        Color(0xFF91F5D8)
    }
    val handleColor = if (vm.isDarkTheme) {
        Color(0xFF646464)
    } else {
        Color(0xFF96B5D2)
    }

    // ------------------------------------------------------------


    val context = LocalContext.current

    // 1) Получаем ориентацию через LocalConfiguration
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    // 2) Определяем максимальную длину строки
    val maxLength = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        50  // горизонтальная ориентация
    } else {
        25  // портретная ориентация
    }

    // 3) Готовим обрезанное название
    val rawTitle = vm.getSelectedTitle()
    val truncatedTitle = if (rawTitle.length > maxLength) {
        rawTitle.take(maxLength) + "..."
    } else {
        rawTitle
    }

    val buttonContainerColor = if (vm.isDarkTheme) {
        Color.DarkGray
    } else {
        // Пусть будет #cebfa8 (слегка потемнее, чем фон #eae2d5)
        Color(0xFFCEBFA8) // фон кнопок/блоков
    }
    val buttonContentColor = if (vm.isDarkTheme) {
        Color.White
    } else {
        // Тёмно-коричневый
        Color(0xFF442B1A)
    }


    // --- 2) Заводим ScrollState
    val scrollState = rememberScrollState()

    // --- 3) Храним высоты: «высота контента» и «высота viewport» (в px).
    val totalContentHeightPx = remember { mutableStateOf(0f) }
    val viewportHeightPx     = remember { mutableStateOf(0f) }

    LaunchedEffect(totalContentHeightPx.value, viewportHeightPx.value) {
        // Печатаем в лог для отладки
        //Log.d("ScrollDebug", "totalContentHeightPx=${totalContentHeightPx.value}, viewportHeightPx=${viewportHeightPx.value}")
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // (1) Кнопка Song: ... в ЛЕВОМ верхнем углу
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Button(
                onClick = { navController.navigate("page2") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = songButtonContainer,
                    contentColor = songButtonContent
                )
            ) {
                Text("Song: $truncatedTitle")
            }
        }

        // Кнопка Light/Dark в правом верхнем углу
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Button(
                onClick = { vm.toggleDarkTheme(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeButtonContainer,
                    contentColor = themeButtonContent
                )
            ) {
                Text(if (vm.isDarkTheme) "Light" else "Dark")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Отступ сверху (чтобы не «наезжать» на верхние кнопки)
            Spacer(modifier = Modifier.height(80.dp))

            // (3A) Box под список (по высоте = weight(1f)), + скроллбар
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // Здесь мы измеряем «высоту окна» (т. е. сколько места отведено под список)
                    .onGloballyPositioned { coords ->
                        viewportHeightPx.value = coords.size.height.toFloat()
                    }
            ) {

                val realHeightPx = min(totalContentHeightPx.value, viewportHeightPx.value)
                val realHeightDp = with(LocalDensity.current) { realHeightPx.toDp() }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(realHeightDp)
                        .background(settingsAreaBgColor)
                )

                // (3B) Прокручиваемый Column
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxSize()

                        // Когда контент отрисован, узнаём его полную высоту
                        .onGloballyPositioned { coords ->
                            val contentH = coords.size.height.toFloat()
                            totalContentHeightPx.value = contentH
                        }
                ) {
                // Start guitar

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,     // центрируем по горизонтали
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Start guitar",
                            color = textColor,
                            modifier = Modifier.clickable {
                                vm.updateStartGuitar(context, !vm.startGuitar)
                            }
                        )
                        Checkbox(
                            checked = vm.startGuitar,
                            onCheckedChange = { newValue ->
                                vm.updateStartGuitar(context, newValue)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = checkBoxBoxColor,
                                uncheckedColor = checkBoxBoxColor,
                                checkmarkColor = checkMarkColor
                            )
                        )
                    }


                // Music on phone

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,     // центрируем по горизонтали
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Music on phone",
                            color = textColor,
                            modifier = Modifier.clickable {
                                vm.updateMusicOnPhone(context, !vm.musicOnPhone)
                            }
                        )
                        Checkbox(
                            checked = vm.musicOnPhone,
                            onCheckedChange = { newValue ->
                                vm.updateMusicOnPhone(context, newValue)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = checkBoxBoxColor,
                                uncheckedColor = checkBoxBoxColor,
                                checkmarkColor = checkMarkColor
                            )
                        )
                    }


                // Tempo

                    // В одной строке — надпись Tempo: и 3 "кнопки"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Добавляем дополнительные отступы сверху и снизу:
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tempo:", color = textColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        listOf(1.0f, 0.75f, 0.5f).forEach { t ->
                            val label = when (t) {
                                1.0f  -> "100%"
                                0.75f -> "75%"
                                else  -> "50%"
                            }
                            Box(
                                modifier = Modifier
                                    .clickable { vm.updateTempo(context, t) }
                                    .background(
                                        if (vm.tempo == t) buttonContainerColor else Color.Transparent
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(label, color = textColor)
                            }
                        }
                    }


                // Vibration

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vibration:", color = textColor,
                            modifier = Modifier.clickable {
                                vm.updateVibrationOn(context, !vm.vibrationOn)
                            }
                        )
                        Checkbox(
                            checked = vm.vibrationOn,
                            onCheckedChange = { newVal ->
                                vm.updateVibrationOn(context, newVal)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = checkBoxBoxColor,
                                uncheckedColor = checkBoxBoxColor,
                                checkmarkColor = checkMarkColor
                            )
                        )
                    }


                // Demo mode

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Demo mode:", color = textColor,
                            modifier = Modifier.clickable {
                                vm.updateDemoMode(context, !vm.demoMode)
                            }
                        )
                        Checkbox(
                            checked = vm.demoMode,
                            onCheckedChange = { vm.updateDemoMode(context, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = checkBoxBoxColor,
                                uncheckedColor = checkBoxBoxColor,
                                checkmarkColor = checkMarkColor
                            )
                        )
                    }

            } // конец LazyColumn





                val contentH = totalContentHeightPx.value.coerceAtLeast(1f)
                val viewH    = viewportHeightPx.value.coerceAtLeast(1f)
                // Если всё умещается => НЕ рисуем скроллбар
                // (contentH <= viewH значит прокрутки не нужно)
                if (contentH > viewH) {

                val scrollPx  = scrollState.value.toFloat()
                val maxScroll = scrollState.maxValue.toFloat().coerceAtLeast(0f)

                val fractionScroll = if (maxScroll > 0f) (scrollPx / maxScroll) else 0f
                val fractionVisible = (viewH / contentH).coerceIn(0f, 1f)

                // Трек: по высоте совпадает с Box => matchParentSize().width(8.dp)
                Box(
                    modifier = Modifier
                        .matchParentSize()    // покрываем весь Box
                ) {
                    // Сама полоска (track)
                    // пусть она прижата к правому краю, занимая всю высоту
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(8.dp)
                            .fillMaxHeight() // высота = высота Box
                            .background(trackColor)
                    )

                    // Ручка (handle)
                    // Высота = fractionVisible * (высота Box)
                    // offsetY = fractionScroll * (BoxHeight - handleHeight)
                    // => для вычисления px нужна onGloballyPositioned или matchParentSize + offset
                    // Можно через .align(...), но проще .offset() в px.

                    // Для вычисления offset() придётся знать «boxHeightPx».
                    // Можем ещё раз onGloballyPositioned(...). Или же layout-фазы.
                    // Проще: завести ещё один вспомогательный State, куда пишем высоту Box (viewportHeightPx).
                    val boxHeightPx = viewportHeightPx.value

                    val handleHeightPx = fractionVisible * boxHeightPx
                    // trackHeightPx = boxHeightPx
                    val handleTopPx    = fractionScroll * (boxHeightPx - handleHeightPx)

                    // Рисуем ручку
                    Box(
                        modifier = Modifier
                            .offset {
                                // offset = (x=0, y= handleTopPx)
                                // offset принимает IntOffset => округлим
                                val offsetY = handleTopPx.toInt()
                                androidx.compose.ui.unit.IntOffset(x = 0, y = offsetY)
                            }
                            .align(Alignment.TopEnd)
                            .width(8.dp)
                            .height(
                                with(LocalDensity.current) {
                                    handleHeightPx.toDp()
                                }
                            )
                            .background(handleColor)
                    )
                }
                    }
            }



            // --- (3) В самом низу — кнопка "Play" ---
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    vm.startPlaying(context)
                    navController.navigate("page3")
                },
                modifier = Modifier
                    .width(200.dp)                     // Ширина кнопки
                    .align(Alignment.CenterHorizontally)  // Центровка по горизонтали
                    .offset(y = (-20).dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = playButtonContainer,
                    contentColor = playButtonContent
                )
            ) {
                Text("Play")
            }
        } // конец Column
    }
}


// Небольшая вспомогательная "радиокнопка", стилизованная как Button
@Composable
fun PlaybackRadioOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) containerColor else Color.Transparent,
            contentColor = if (isSelected) contentColor else containerColor
        ),
        border = null
    ) {
        Text(label)
    }
}
