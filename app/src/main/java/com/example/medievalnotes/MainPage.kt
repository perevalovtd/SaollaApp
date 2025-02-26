package com.example.medievalnotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Кнопка темы (как и было)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {

            // =========== НОВАЯ СЕКЦИЯ: Playback (App/Guitar) ===========
            // Заголовок "Playback"
            Text("Playback", color = textColor)

            // Ряд из двух "радиокнопок": App слева, Guitar справа
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Кнопка App
                PlaybackRadioOption(
                    label = "App",
                    isSelected = (vm.playbackMode == PlaybackMode.APP),
                    onClick = { vm.changePlaybackMode(context, PlaybackMode.APP) },
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                )

                // Кнопка Guitar
                PlaybackRadioOption(
                    label = "Guitar",
                    isSelected = (vm.playbackMode == PlaybackMode.GUITAR),
                    onClick = { vm.changePlaybackMode(context, PlaybackMode.GUITAR) },
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                )
            }
            // =========== КОНЕЦ НОВОЙ СЕКЦИИ ===========

            // Song
            Button(
                onClick = { navController.navigate("page2") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                )
            ) {
                Text("Song: ${vm.getSelectedTitle()}")
            }

            // Tempo
            Text("Tempo:", color = textColor)
            Row {
                listOf(1.0f, 0.75f, 0.5f).forEach { t ->
                    val label = when (t) {
                        1.0f -> "100%"
                        0.75f -> "75%"
                        else -> "50%"
                    }
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
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

            // Demo mode
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

            // Play => startPlaying => переход page3
            Button(
                onClick = {
                    vm.startPlaying(context)
                    navController.navigate("page3")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                )
            ) {
                Text("Play")
            }
        }
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
