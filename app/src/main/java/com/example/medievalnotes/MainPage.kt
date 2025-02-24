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
        // Кнопка темы
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Button(
                onClick = { vm.toggleDarkTheme() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeButtonContainer,
                    contentColor = themeButtonContent
                )
            ) {
                Text(themeButtonText)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
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
                            .clickable { vm.updateTempo(t) }
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
                        vm.updateDemoMode(!vm.demoMode)
                    }
                )
                Checkbox(
                    checked = vm.demoMode,
                    onCheckedChange = { vm.updateDemoMode(it) },
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
