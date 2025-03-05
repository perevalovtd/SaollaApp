package com.example.medievalnotes

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController



@Composable
fun MainPage(
    navController: NavHostController,
    vm: MainViewModel
) {
    val backgroundColor = if (vm.isDarkTheme) Color.Black else Color.White
    val textColor = if (vm.isDarkTheme) Color.White else Color.Black
    val buttonContainerColor = if (vm.isDarkTheme) Color.DarkGray else Color.LightGray
    val buttonContentColor = if (vm.isDarkTheme) Color.White else Color.Black

    val themeButtonText = if (vm.isDarkTheme) "Light" else "Dark"
    val themeButtonContainer = if (vm.isDarkTheme) Color.White else Color.Black
    val themeButtonContent = if (vm.isDarkTheme) Color.Black else Color.White

    val checkBoxBoxColor = if (vm.isDarkTheme) Color.DarkGray else Color.Black
    val checkMarkColor = Color.White

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
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
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

        // Общая колонка на весь экран
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),       // боковые отступы при желании
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Добавим Spacer, чтобы настройки не «наезжали» на верхние кнопки
            Spacer(modifier = Modifier.height(80.dp))

            // --- (2) Прокручиваемая область со всеми настройками ---
            // Задаём вес (weight(1f)), чтобы занять «среднюю» часть экрана,
            // и используем LazyColumn (или Column + verticalScroll).
            LazyColumn(
                modifier = Modifier
                    .weight(1f)            // всё лишнее место займёт этот список
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start guitar
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                }

                // Music on phone
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                }

                // Tempo
                item {
                    // В одной строке — надпись Tempo: и 3 "кнопки"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Tempo:", color = textColor)
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
                }

                // Vibration
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                }

                // Demo mode
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                }
            } // конец LazyColumn

            // --- (3) В самом низу — кнопка "Play" ---
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    vm.startPlaying(context)
                    navController.navigate("page3")
                },
                modifier = Modifier
                    .width(200.dp)                     // Ширина кнопки
                    .align(Alignment.CenterHorizontally),  // Центровка по горизонтали
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
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
